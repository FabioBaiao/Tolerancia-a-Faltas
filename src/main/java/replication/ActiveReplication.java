package replication;

import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.SerializableTypeResolver;
import io.atomix.catalyst.serializer.Serializer;
import pt.haslab.ekit.Spread;
import rmi.Rep;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ActiveReplication {

    private int id;
    private String groupName;
    private Spread s;

    private ThreadContext tc;

    private CompletableFuture<ActiveReplication> opened;
    private CompletableFuture<ActiveReplication> updated;

    public ActiveReplication(int id, boolean groupMembership, SerializableTypeResolver tr) throws SpreadException {
        this.id = id;
        this.groupName = "servers";
        this.s = new Spread("srv-" + id, groupMembership);

        this.tc = new SingleThreadContext("srv-%d", new Serializer(tr));

        tc.serializer()
                .register(ReqState.class)
                .register(RepState.class);
    }

    public CompletableFuture<ActiveReplication> open() {
        if (opened != null)
            return opened;

        opened = new CompletableFuture<>();

        tc.execute(() -> s.open()).join().thenRun(() -> {
            s.join(groupName);
            opened.complete(this);
        });

        return opened;
    }

    public CompletableFuture<ActiveReplication> update
            (List<Class<?>> types,
             Consumer<State> setState,
             Supplier<State> getState,
             Map<Class<?>, BiConsumer<SpreadMessage, Object>> updateFunctions) {

        if (updated != null)
            return updated;

        updated = new CompletableFuture<>();

        LinkedList<Tuple<?>> savedObjects = new LinkedList<>();

        // primeiro processo
        if (this.id == 0) {
            finalReqStateHandler(getState);

            updated.complete(this);
        } else {
            // handler para receber ReqStates ate ao replication.ReqState que foi enviado pelo proprio
            s.handler(ReqState.class, (msg, req) -> {
                // considerar outros requests apenas quando receber proprio replication.ReqState
                if (this.id == req.getId()) {
                    for (Class<?> t : types) {
                        saveObjectHandler(t, savedObjects);
                    }

                    saveObjectHandler(ReqState.class, savedObjects);
                }
            });

            // handler para receber resposta
            s.handler(RepState.class, (msg, req) -> {

                // atualizar estado
                setState.accept(req.getState());
                // responder a todos os requests guardados
                for (Tuple<?> t : savedObjects) {
                    Class<?> type = t.getType();
                    if (type.equals(ReqState.class))
                        reply(t.getMsg().getSender(), new RepState(getState.get()));
                    else
                        updateFunctions.get(t.getType()).accept(t.getMsg(), t.getObject());
                }

                finalReqStateHandler(getState);
                // inutilizar este handler
                s.handler(RepState.class, (_msg, _req) -> {});

                updated.complete(this);
            });

            init();
        }

        return updated;
    }

    private void init() {
        SpreadMessage msg = new SpreadMessage();
        msg.addGroup(this.groupName);

        ReqState req = new ReqState(this.id);

        s.multicast(msg, req);
    }

    private <T> void saveObjectHandler(Class<T> t, LinkedList<Tuple<?>> savedObjects) {
        // handler para guardar requests do tipo t ate receber resposta com o estado
        s.handler(t, (msg, obj) -> {
            // guardar request
            savedObjects.add(new Tuple<>(t, msg, obj));
        });
    }

    private void finalReqStateHandler(Supplier<State> getState) {

        // handler para responder com estado
        s.handler(ReqState.class, (msg, req) -> {
            State state = getState.get();

            reply(msg.getSender(), new RepState(state));
        });
    }

    public void reply(SpreadGroup sender, Rep rep) {
        SpreadMessage msg = new SpreadMessage();
        msg.addGroup(sender);

        s.multicast(msg, rep);
    }

    public <T> void handler(Class<T> type, BiConsumer<SpreadMessage, T> rh) {
        s.handler(type, rh);
    }

    public void join(String groupName) {
        s.join(groupName);
    }
}

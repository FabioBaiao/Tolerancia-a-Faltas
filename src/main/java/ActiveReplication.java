import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import pt.haslab.ekit.Spread;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ActiveReplication {

    private int id;
    private String groupName;
    private Spread s;

    private ThreadContext tc;

    private CompletableFuture<ActiveReplication> opened;
    private CompletableFuture<ActiveReplication> updated;

    public ActiveReplication(int id, boolean groupMembership) throws SpreadException {
        this.id = id;
        this.groupName = "servers";
        this.s = new Spread("srv-" + id, groupMembership);

        this.tc = new SingleThreadContext("srv-%d", new Serializer());

        tc.serializer()
                .register(ReqState.class)
                .register(RepState.class);
    }

    public CompletableFuture<ActiveReplication> open() {
        if (opened != null)
            return opened;

        opened = new CompletableFuture<>();

        tc.execute(() -> s.open()).join().join();

        s.join(groupName);

        this.groupName = groupName;

        opened.complete(this);

        return opened;
    }

    public CompletableFuture<ActiveReplication> update
            (List<Class<?>> types, Consumer<State> setState, Function<Void, State> getState, Consumer<Tuple<?>> updateState) {

        if (updated != null)
            return updated;

        updated = new CompletableFuture<>();

        LinkedList<Tuple<?>> savedReqs = new LinkedList<>();

        // primeiro processo
        if (this.id == 0) {
            finalReqStateHandler(getState);

            updated.complete(this);
        }
        else {
            // handler para receber ReqStates ate ao ReqState que foi enviado pelo proprio
            s.handler(ReqState.class, (msg, req) -> {
                // considerar outros requests apenas quando receber proprio ReqState
                if (this.id == req.getId()) {
                    for (Class<?> t : types) {
                        saveReqHandler(t, savedReqs);
                    }

                    saveReqStateHandler(savedReqs);
                }
            });

            // handler para receber resposta
            s.handler(RepState.class, (msg, req) -> {

                // atualizar estado
                setState.accept(req.getState());
                // responder a todos os requests guardados
                for (Tuple<?> t : savedReqs) {
                    updateState.accept(t);
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

    private <T> void saveReqHandler(Class<T> t, LinkedList<Tuple<?>> savedReqs) {
        // handler para guardar requests do tipo t ate receber resposta com o estado
        s.handler(t, (msg, req) -> {
            // guardar request
            savedReqs.add(new Tuple<>(t, msg, (Req) req));
        });
    }

    private void saveReqStateHandler(LinkedList<Tuple<?>> savedReqs) {

        //handler para guardar ReqStates ate receber resposta com o estado
        s.handler(ReqState.class, (msg, req) -> {
            // guardar request
            savedReqs.add(new Tuple<>(ReqState.class, msg, req));
        });
    }

    private void finalReqStateHandler(Function<Void, State> getState) {

        // handler para responder com estado
        s.handler(ReqState.class, (msg, req) -> {
            State state = getState.apply(null);

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

import spread.MembershipInfo;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Server implements Runnable {

    private ActiveReplication ar;

    private TaskScheduler ts;

    public Server(String[] args) throws SpreadException {

        this.ar = new ActiveReplication(Integer.parseInt(args[0]), true);

        this.ts = new TaskScheduler();
    }

    @Override
    public void run() {
        ar.open().join();

        ar.join("all");

        List<Class<?>> types = getTypes();

        Consumer<State> setState = (state) -> this.ts = (TaskScheduler) state;
        Supplier<State> getState = () -> this.ts;

        Map<Class<?>, BiConsumer<SpreadMessage, Object>> updateFunctions = getUpdateFunctions();
        Consumer<Tuple<?>> updateState = (t) -> updateFunctions.get(t.getType()).accept(t.getMsg(), t.getObject());

        ar.update(types, setState, getState, updateState)
                .thenRun(() -> createHandlers());

    }

    private void addTask(SpreadMessage msg, AddTaskReq req) {
        String url = ts.addTask(req.getName(), req.getDescription());

        this.ar.reply(msg.getSender(), new AddTaskRep(url));
    }

    private void assignTask(SpreadMessage msg, AssignTaskReq req) {
        Optional<Task> ot = ts.assignTask(msg.getSender().toString());

        this.ar.reply(msg.getSender(), new AssignTaskRep(ot));
    }

    private void completeTask(SpreadMessage msg, CompleteTaskReq req) {
        Optional<Task> ot = ts.completeTask(msg.getSender().toString(), req.getUrl());

        this.ar.reply(msg.getSender(), new CompleteTaskRep(ot));
    }

    private void unassignAll(SpreadMessage msg, MembershipInfo req) {
        SpreadGroup client;
        if (req.isCausedByDisconnect())
            client = req.getDisconnected();
        else if (req.isCausedByLeave())
            client = req.getLeft();
        else
            return;

        ts.unassignAll(client.toString());
    }

    // lista que contem todos os tipos de requests que podem ser recebidos
    // TODO preencher lista
    private List<Class<?>> getTypes() {
        List<Class<?>> types = new ArrayList<>();
        types.add(AddTaskReq.class);
        types.add(AssignTaskReq.class);
        types.add(CompleteTaskReq.class);
        types.add(MembershipInfo.class);
        return types;
    }

    private Map<Class<?>,BiConsumer<SpreadMessage,Object>> getUpdateFunctions() {
        Map<Class<?>, BiConsumer<SpreadMessage, Object>> updateFunctions = new HashMap<>();

        updateFunctions.put(AddTaskReq.class, (msg, req) -> addTask(msg, (AddTaskReq) req));
        updateFunctions.put(AssignTaskReq.class, (msg, req) -> assignTask(msg, (AssignTaskReq) req));
        updateFunctions.put(CompleteTaskReq.class, (msg, req) -> completeTask(msg, (CompleteTaskReq) req));
        updateFunctions.put(MembershipInfo.class, (msg, req) -> unassignAll(msg, (MembershipInfo) req));

        return updateFunctions;
    }

    private void createHandlers() {

        this.ar.handler(AddTaskReq.class, (msg, req) -> addTask(msg, req));
        this.ar.handler(AssignTaskReq.class, (msg, req) -> assignTask(msg, req));
        this.ar.handler(CompleteTaskReq.class, (msg, req) -> completeTask(msg, req));
        // handler para detetar falhas de clientes
        this.ar.handler(MembershipInfo.class, (msg, req) -> unassignAll(msg, req));
    }

    public static void main(String[] args) {

        try {

            new Thread(new Server(args)).start();


        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }

}

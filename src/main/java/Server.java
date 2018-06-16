import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import pt.haslab.ekit.Spread;
import replication.AckState;
import replication.RepState;
import replication.Tuple;
import rmi.*;
import serializers.ServerTaskSchedulingTypeResolver;
import spread.MembershipInfo;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;
import tasks.Task;
import tasks.TaskScheduler;
import tasks.TaskSchedulerImpl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class Server implements Runnable {

    private SpreadGroup serversGroup;
    private SpreadGroup globalGroup;

    private String privateGroupName;
    private Spread s;
    private ThreadContext tc;

    private String leader;

    private TaskScheduler ts;

    Map<String, MembershipInfo> infos;

    public Server(String[] args) throws SpreadException {
        String privateName = args[0];

        this.s = new Spread(privateName, true);
        this.tc = new SingleThreadContext("srv-%d", new Serializer(new ServerTaskSchedulingTypeResolver()));

        this.ts = new TaskSchedulerImpl();

        this.infos = new HashMap<>();
    }

    @Override
    public void run() {
        waitOwnJoinedMembership();

        tc.execute(() -> s.open()).join().thenRun(() -> {
            this.privateGroupName = s.getPrivateGroup().toString();
            serversGroup = s.join("servers");
            globalGroup = s.join("global");
        });
    }

    private void waitOwnJoinedMembership() {
        s.handler(MembershipInfo.class, (msg, info) -> {

            SpreadGroup group = info.getGroup();
            if (group.equals(serversGroup) && info.isCausedByJoin()) {

                String joined = info.getJoined().toString();
                if (joined.equals(privateGroupName)) {

                    // unico membro
                    if (info.getMembers().length == 1) {

                        leader = privateGroupName;
                        finalHandlers();
                    }
                    else {
                        LinkedList<Tuple<?>> savedMessages = new LinkedList<>();

                        saveRequestsHandlers(savedMessages);
                        receiveStateHandler(savedMessages);
                    }
                }
            }
        });
    }

    private void receiveStateHandler(LinkedList<Tuple<?>> savedMessages) {
        s.handler(RepState.class, (msg, rep) -> {
            send(serversGroup, new AckState());

            ts = rep.getTaskScheduler();
            leader = rep.getLeader();

            Map<Class<?>, BiConsumer<SpreadMessage, Object>> updateFunctions = getUpdateFunctions();

            for (Tuple<?> t : savedMessages) {
                updateFunctions.get(t.getType()).accept(t.getMsg(), t.getObject());
            }

            finalHandlers();

            s.handler(RepState.class, (_msg, _rep) -> {});
        });
    }

    private void saveRequestsHandlers(LinkedList<Tuple<?>> savedMessages) {
        s.handler(AckState.class, (msg, ack) -> savedMessages.add(new Tuple<>(AckState.class, msg, ack)));
        s.handler(AddTaskReq.class, (msg, req) -> savedMessages.add(new Tuple<>(AddTaskReq.class, msg, req)));
        s.handler(AssignTaskReq.class, (msg, req) -> savedMessages.add(new Tuple<>(AssignTaskReq.class, msg, req)));
        s.handler(CompleteTaskReq.class, (msg, req) -> savedMessages.add(new Tuple<>(CompleteTaskReq.class, msg, req)));
        s.handler(MembershipInfo.class, (msg, info) -> savedMessages.add(new Tuple<>(MembershipInfo.class, msg, info)));
        s.handler(UnassignAll.class, (msg, op) -> savedMessages.add(new Tuple<>(UnassignAll.class, msg, op)));
    }

    private Map<Class<?>, BiConsumer<SpreadMessage, Object>> getUpdateFunctions() {
        Map<Class<?>, BiConsumer<SpreadMessage, Object>> updateFunctions = new HashMap<>();

        updateFunctions.put(AckState.class, (msg, ack) -> ackState(msg, (AckState) ack));
        updateFunctions.put(AddTaskReq.class, (msg, req) -> addTask(msg, (AddTaskReq) req));
        updateFunctions.put(AssignTaskReq.class, (msg, req) -> assignTask(msg, (AssignTaskReq) req));
        updateFunctions.put(CompleteTaskReq.class, (msg, req) -> completeTask(msg, (CompleteTaskReq) req));
        updateFunctions.put(MembershipInfo.class, (msg, info) -> membershipInfo(msg, (MembershipInfo) info));
        updateFunctions.put(UnassignAll.class, (msg, op) -> unassignAll(msg, (UnassignAll) op));

        return updateFunctions;
    }

    private void finalHandlers() {
        s.handler(AckState.class, this::ackState);
        s.handler(AddTaskReq.class, this::addTask);
        s.handler(AssignTaskReq.class, this::assignTask);
        s.handler(CompleteTaskReq.class, this::completeTask);
        s.handler(MembershipInfo.class, this::membershipInfo);
        s.handler(UnassignAll.class, this::unassignAll);
    }

    private void ackState(SpreadMessage msg, AckState ack) {
        // remover info guardada
        infos.remove(msg.getSender().toString());
    }

    private void addTask(SpreadMessage msg, AddTaskReq req) {
        String url = ts.addTask(req.getName(), req.getDescription(), req.getCreationDateTime());

        send(msg.getSender(), new AddTaskRep(req.getId(), url));
    }

    private void assignTask(SpreadMessage msg, AssignTaskReq req) {
        Optional<Task> ot = ts.assignTask(msg.getSender().toString());

        send(msg.getSender(), new AssignTaskRep(req.getId(), ot));
    }

    private void completeTask(SpreadMessage msg, CompleteTaskReq req) {
        Optional<Task> ot = ts.completeTask(msg.getSender().toString(), req.getUrl(), req.getCompletionDateTime());

        send(msg.getSender(), new CompleteTaskRep(req.getId(), ot));
    }

    private void membershipInfo(SpreadMessage msg, MembershipInfo info) {
        SpreadGroup group = info.getGroup();
        if (group.equals(serversGroup)) {

            if (info.isCausedByJoin()) {

                if (leader.equals(privateGroupName)) {

                    SpreadGroup joined = info.getJoined();

                    send(joined, new RepState(ts, leader));

                    // guardar info ate receber confirmacao
                    infos.put(joined.toString(), info);
                }
            }
            else if (info.isCausedByDisconnect()) {

                String disconnected = info.getDisconnected().toString();
                if (disconnected.equals(leader)) {

                    leaderElection(info.getMembers());

                    if (leader.equals(privateGroupName)) {

                        // reenviar infos guardadas
                        for (MembershipInfo mi : infos.values()) {
                            membershipInfo(null, mi);
                        }
                    }
                }
            }
        }
        else if (group.equals(globalGroup)) {

            if (info.isCausedByDisconnect()) {

                String disconnected = info.getDisconnected().toString();

                if (leader.equals(privateGroupName)) {

                    send(serversGroup, new UnassignAll(disconnected));
                }

                // guardar info ate receber request de unassignAll
                infos.put(disconnected, info);
            }
        }
    }

    private void unassignAll(SpreadMessage msg, UnassignAll op) {
        ts.unassignAll(op.getDisconnected());

        // remover info guardada
        infos.remove(op.getDisconnected());
    }

    private void leaderElection(SpreadGroup[] members) {
        leader = privateGroupName;

        for (SpreadGroup m : members) {
            String member = m.toString();
            if (member.compareTo(leader) < 0 && !infos.containsKey(member)) {
                leader = member;
            }
        }
    }

    private void send(SpreadGroup destination, Object obj) {
        SpreadMessage msg = new SpreadMessage();
        msg.addGroup(destination);
        msg.setAgreed();

        s.multicast(msg, obj);
    }

    public static void main(String[] args) {
        try {

            new Server(args).run();

        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }

}

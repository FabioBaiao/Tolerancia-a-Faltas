package tasks;

import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import pt.haslab.ekit.Spread;
import rmi.*;
import serializers.TaskSchedulingTypeResolver;
import spread.SpreadException;
import spread.SpreadMessage;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TaskSchedulerStub implements TaskScheduler {

    //private final String groupName;
    private final Spread s;
    private final ThreadContext tc;

    private CompletableFuture<Rep> futureResult; // future result of current request
    private int waitingId;
    private int reqId;

    private TaskSchedulerStub(String privateGroupName) throws SpreadException {
        //this.groupName = "all";
        this.s = new Spread(privateGroupName, false);
        this.tc = new SingleThreadContext("cli-%d", new Serializer(new TaskSchedulingTypeResolver()));

        this.waitingId = -1;
        this.reqId = 0;
    }

    public static TaskSchedulerStub newInstance(String privateGroupName) throws SpreadException {
        TaskSchedulerStub instance = new TaskSchedulerStub(privateGroupName);

        instance.registerHandlers();
        instance.open();
        instance.join("all");

        return instance;
    }

    private void registerHandlers() {

        s.handler(AddTaskRep.class, (msg, rep) -> receive(rep));
        s.handler(AssignTaskRep.class, (msg, rep) -> receive(rep));
        s.handler(CompleteTaskRep.class, (msg, rep) -> receive(rep));
    }

    private void open() throws SpreadException {
        try {
            tc.execute(s::open).join().get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex);
        } catch (ExecutionException ex) {
            throw (SpreadException) ex.getCause();
        }
    }

    private void join(String groupName) {
        s.join(groupName);
    }

    private void receive(Rep rep) {
        if (rep.getId() == waitingId) {
            futureResult.complete(rep);
            this.waitingId = -1;
        }
    }

    private Rep sendAndReceive(Req req) {
        this.futureResult = new CompletableFuture<>();
        this.waitingId = this.reqId;
        req.setId(this.reqId++);

        SpreadMessage msg = new SpreadMessage();
        msg.addGroup("servers");
        s.multicast(msg, req);

        return this.futureResult.join();
    }

    @Override
    public String addTask(String name, String description, LocalDateTime creationDateTime) {
        AddTaskReq req = new AddTaskReq(name, description, creationDateTime);

        AddTaskRep rep = (AddTaskRep) sendAndReceive(req);
        return rep.getUrl();
    }

    @Override
    public Optional<Task> assignTask(String privateGroupName) {
        AssignTaskReq req = new AssignTaskReq(privateGroupName);

        AssignTaskRep rep = (AssignTaskRep) sendAndReceive(req);
        return rep.getTask();
    }

    @Override
    public void unassignAll(String privateGroupName) {
        throw new UnsupportedOperationException("To be implemented");
    }

    @Override
    public Optional<Task> completeTask(String privateGroupName, String url, LocalDateTime completionDateTime) {
        CompleteTaskReq req = new CompleteTaskReq(privateGroupName, url, completionDateTime);

        CompleteTaskRep rep = (CompleteTaskRep) sendAndReceive(req);
        return rep.getTask();
    }
}
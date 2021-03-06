package tasks;

import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.haslab.ekit.Spread;
import rmi.*;
import serializers.BaseTaskSchedulingTypeResolver;
import spread.SpreadException;
import spread.SpreadMessage;

import java.io.Closeable;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TaskSchedulerStub implements TaskScheduler, Closeable {

    private static final Logger logger = LoggerFactory.getLogger(TaskSchedulerStub.class);

    //private final String groupName;
    private final Spread s;
    private final ThreadContext tc;

    private CompletableFuture<Rep> futureResult; // future result of current request
    private int waitingId;
    private int reqId;

    private TaskSchedulerStub(String privateName) throws SpreadException {
        //this.groupName = "all";
        this.s = new Spread(privateName, false);
        this.tc = new SingleThreadContext("cli-%d", new Serializer(new BaseTaskSchedulingTypeResolver()));

        this.waitingId = -1;
        this.reqId = 0;
    }

    public static TaskSchedulerStub newInstance(String privateName) throws SpreadException {
        TaskSchedulerStub instance = new TaskSchedulerStub(privateName);

        instance.registerHandlers();
        instance.open();
        instance.join("global");

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
        this.waitingId = this.reqId++;

        SpreadMessage msg = new SpreadMessage();
        msg.addGroup("servers");
        msg.setAgreed();
        s.multicast(msg, req);

        return this.futureResult.join();
    }

    @Override
    public String addTask(String name, String description, LocalDateTime creationDateTime) {
        AddTaskReq req = new AddTaskReq(this.reqId, name, description, creationDateTime);

        AddTaskRep rep = (AddTaskRep) sendAndReceive(req);
        return rep.getUrl();
    }

    @Override
    public Optional<Task> assignTask(String username) {
        AssignTaskReq req = new AssignTaskReq(this.reqId, username);

        AssignTaskRep rep = (AssignTaskRep) sendAndReceive(req);
        return rep.getTask();
    }

    @Override
    public void unassignAll(String username) {
        throw new UnsupportedOperationException("To be implemented");
    }

    @Override
    public Optional<Task> completeTask(String username, String url, LocalDateTime completionDateTime) {
        CompleteTaskReq req = new CompleteTaskReq(this.reqId, username, url, completionDateTime);

        CompleteTaskRep rep = (CompleteTaskRep) sendAndReceive(req);
        return rep.getTask();
    }

    public void close() {
        try {
            tc.execute(s::close).join().get();
        } catch (ExecutionException ex) {
            logger.error(null, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex);
        } finally {
            tc.close();
        }
    }
}

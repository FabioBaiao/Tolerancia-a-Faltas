package rmi;

import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import pt.haslab.ekit.Spread;
import serializers.TaskSchedulingTypeResolver;
import spread.SpreadException;
import tasks.Task;
import tasks.TaskScheduler;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TaskSchedulerStub implements TaskScheduler {

    private final String groupName;
    private final Spread s;
    private final ThreadContext tc;

    private CompletableFuture<Object> futureResult; // future result of current request

    private TaskSchedulerStub(String privateGroupName) throws SpreadException {
        this.groupName = "all";
        this.s = new Spread(privateGroupName, false);
        this.tc = new SingleThreadContext("cli-%d", new Serializer(new TaskSchedulingTypeResolver()));
    }

    public static TaskSchedulerStub newInstance(String privateGroupName) throws SpreadException {
        TaskSchedulerStub instance = new TaskSchedulerStub(privateGroupName);

        instance.registerHandlers();
        instance.open();

        return instance;
    }

    private void registerHandlers() {
        throw new UnsupportedOperationException("To be implemented");
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

    @Override
    public String addTask(String name, String description, LocalDateTime creationDateTime) {
        throw new UnsupportedOperationException("To be implemented");
    }

    @Override
    public Optional<Task> assignTask(String privateGroupName) {
        throw new UnsupportedOperationException("To be implemented");
    }

    @Override
    public void unassignAll(String privateGroupName) {
        throw new UnsupportedOperationException("To be implemented");
    }

    @Override
    public Optional<Task> completeTask(String privateGroupName, String url, LocalDateTime completionDateTime) {
        throw new UnsupportedOperationException("To be implemented");
    }
}

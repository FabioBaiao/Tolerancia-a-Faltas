package tasks;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;
import replication.State;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;

// Not thread safe
public class TaskSchedulerImpl extends State implements TaskScheduler {

    private static long nextTaskId = 0;

    /**
     * Unassigned tasks, ordered from oldest to the newest.
     */
    private PriorityQueue<Task> unassignedTasks;

    /**
     * Maps each client's private group name to the tasks assigned to that client.
     */
    private Map<String, Map<String, Task>> assignedTasks;

    public TaskSchedulerImpl() {
        this.unassignedTasks = new PriorityQueue<>();
        this.assignedTasks = new HashMap<>();
    }

    private String generateURL() {
        throw new UnsupportedOperationException("To be implemented");
    }

    public String addTask(String name, String description, LocalDateTime creationDateTime) {
        long id = nextTaskId++;
        String url = generateURL();

        unassignedTasks.add(new Task(id, url, name, description, creationDateTime));
        return url;
    }

    public Optional<Task> assignTask(String privateGroupName) {
        Task task = unassignedTasks.poll();

        if (task == null) {
            return Optional.empty();
        }
        Map<String, Task> userTasks = assignedTasks.computeIfAbsent(privateGroupName, k -> new HashMap<>());

        userTasks.put(task.getUrl(), task);
        return Optional.of(task);
    }

    public void unassignAll(String privateGroupName) {
        Map<String, Task> userTasks = assignedTasks.remove(privateGroupName);

        if (userTasks != null) {
            unassignedTasks.addAll(userTasks.values());
            userTasks.clear();
        }
    }

    public Optional<Task> completeTask(String privateGroupName, String url, LocalDateTime completionDateTime) {
        Map<String, Task> userTasks = assignedTasks.get(privateGroupName);

        if (userTasks == null) {
            return Optional.empty();
        }
        Task task = userTasks.remove(url);

        if (task == null) {
            return Optional.empty();
        }
        task.complete(completionDateTime);

        return Optional.of(task);
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        serializer.writeObject(unassignedTasks, bufferOutput);
        serializer.writeObject(assignedTasks, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        this.unassignedTasks = serializer.readObject(bufferInput);
        this.assignedTasks = serializer.readObject(bufferInput);
    }
}

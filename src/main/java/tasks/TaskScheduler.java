package tasks;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TaskScheduler {

    String addTask(String name, String description, LocalDateTime creationDateTime);

    Optional<Task> assignTask(String privateGroupName);

    void unassignAll(String privateGroupName);

    Optional<Task> completeTask(String privateGroupName, String url, LocalDateTime completionDateTime);
}

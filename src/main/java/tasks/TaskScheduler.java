package tasks;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TaskScheduler {

    String addTask(String name, String description, LocalDateTime creationDateTime);

    Optional<Task> assignTask(String username);

    void unassignAll(String username);

    Optional<Task> completeTask(String username, String url, LocalDateTime completionDateTime);
}

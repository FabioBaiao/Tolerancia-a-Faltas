import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spread.SpreadException;
import tasks.Task;
import tasks.TaskScheduler;
import tasks.TaskSchedulerStub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Client implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private static final String PROMPT = "$ ";
    private static final String SEP = "________________________________________";

    private static final String HELP =
            "addTask             add a new task\n" +
            "nextTask            get the next unassigned task\n" +
            "list                list your tasks\n" +
            "complete taskURL    mark the specified task as complete\n" +
            "help                print this help\n" +
            "exit                exit the app";

    private final String privateName;
    private final TaskScheduler taskScheduler;
    private final BufferedReader in;
    private final Map<String, CheckedIOFunction<String[], Integer>> commandMap;

    private boolean exit;

    public Client(String privateName) throws SpreadException {
        this.privateName = privateName;
        this.taskScheduler = TaskSchedulerStub.newInstance(privateName);
        this.in = new BufferedReader(new InputStreamReader(System.in));

        this.commandMap = new HashMap<>();
        this.commandMap.put("addTask", this::addTask);
        this.commandMap.put("nextTask", this::nextTask);
        this.commandMap.put("list", this::list);
        this.commandMap.put("complete", this::complete);
        this.commandMap.put("help", this::help);
        this.commandMap.put("exit", this::exit);

        this.exit = false;
    }

    public static void main(String[] args) throws SpreadException {
        if (args.length != 1) {
            System.out.println("Usage: Client name");
            System.exit(1);
        }

        new Client(args[0]).run();
    }

    @Override
    public void run() {
        try {
            while (!exit) {
                System.out.print(PROMPT);

                String line = in.readLine();
                String[] args = (line == null) ? new String[]{"exit"} : line.split("[ \t]+");

                commandMap.getOrDefault(args[0], this::handleInvalidCommand).apply(args);
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        } finally {
            close();
        }
    }

    public Integer addTask(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: addTask");
            return -1;
        }
        System.out.print("Task name: ");
        String name = in.readLine();

        System.out.print("Description: ");
        String description = in.readLine();

        String url = taskScheduler.addTask(name, description, LocalDateTime.now());
        System.out.println("Task '" + name + "' successfully registered. Task URL: " + url);
        return 0;
    }

    public Integer nextTask(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: nextTask");
            return -1;
        }
        Optional<Task> maybeTask = taskScheduler.assignTask(privateName);

        if (maybeTask.isPresent()) {
            Task task = maybeTask.get();

            System.out.println("Assigned task");
            System.out.println(SEP);
            System.out.println("URL: " + task.getUrl());
            System.out.println("Name: " + task.getName());
            System.out.println("Description: " + task.getDescription());
            System.out.println("Creation date-time: " + task.getCreationDateTime());
            System.out.println(SEP);
        } else {
            System.out.println("Currently there are no unassigned tasks");
        }
        return 0;
    }

    public Integer list(String[] args) {
        throw new UnsupportedOperationException("To be implemented (not prioritary)");
    }

    public Integer complete(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: complete taskURL");
            return -1;
        }
        String url = args[1];

        Optional<Task> maybeTask = taskScheduler.completeTask(privateName, url, LocalDateTime.now());
        if (maybeTask.isPresent()) {
            System.out.println("Completed task '" + maybeTask.get().getName() + "'");
        } else {
            System.out.println("Task '" + url + "' is not assigned to you");
        }
        return 0;
    }

    public Integer help(String[] args) {
        System.out.println(HELP);
        return 0;
    }

    public Integer handleInvalidCommand(String[] args) {
        System.out.println("Invalid command '" + args[0] + "'. Try 'help'");
        return 0;
    }

    public Integer exit(String[] args) {
        this.exit = true;
        return 0;
    }

    private void close() {
        if (taskScheduler instanceof TaskSchedulerStub) {
            TaskSchedulerStub stub = (TaskSchedulerStub) taskScheduler;
            stub.close();
        }
    }
}

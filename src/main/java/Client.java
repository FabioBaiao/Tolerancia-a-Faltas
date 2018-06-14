import rmi.TaskSchedulerStub;
import spread.SpreadException;
import tasks.Task;
import tasks.TaskScheduler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// TODO: Close stub on exit
// TODO: Complete methods
public class Client implements Runnable {

    private static final String PROMPT = "$ ";
    private static final String SEP = "________________________________________";
    private static final String HELP =
            "addTask             add a new task\n" +
            "nextTask            get the next unassigned task\n" +
            "list                list your tasks\n" +
            "complete taskURL    mark the specified task as complete\n" +
            "help                print this help\n" +
            "exit                exit the app";

    private final String privateGroupName;
    private final TaskScheduler taskScheduler;
    private final BufferedReader in;
    private final Map<String, CheckedIOFunction<String[], Integer>> commandMap;

    private boolean exit;

    public Client(String name) throws SpreadException {
        this.privateGroupName = name;
        this.taskScheduler = TaskSchedulerStub.newInstance(name);
        this.in = new BufferedReader(new InputStreamReader(System.in));

        this.commandMap = new HashMap<>();
        this.commandMap.put("addTask", this::addTask);
        this.commandMap.put("nextTask", this::nextTask);
        this.commandMap.put("list", this::list);
        this.commandMap.put("done", this::done);
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
                String[] args = (line == null) ? new String[]{"exit"} : line.split("[ \t]");

                commandMap.getOrDefault(args[0], this::handleInvalidCommand).apply(args);
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
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

        taskScheduler.addTask(name, description, LocalDateTime.now());
        return 0;
    }

    public Integer nextTask(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: nextTask");
            return -1;
        }
        Optional<Task> maybeTask = taskScheduler.assignTask(privateGroupName);

        if (maybeTask.isPresent()) {
            Task task = maybeTask.get();

            System.out.println("Assigned task");
            System.out.println(SEP);
            System.out.println("URL: " + task.getUrl());
            System.out.println("name: " + task.getName());
            System.out.println("description: " + task.getDescription());
            System.out.println("creation date-time: " + task.getCreationDateTime());
            System.out.println(SEP);
        } else {
            System.out.println("Currently there are no unassigned tasks");
        }
        return 0;
    }

    public Integer list(String[] args) throws IOException {
        throw new UnsupportedOperationException("To be implemented (not prioritary)");
    }

    public Integer done(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: done taskURL");
            return -1;
        }
        String url = args[1];

        taskScheduler.completeTask(privateGroupName, url, LocalDateTime.now());
        return 0;
    }

    public Integer help(String[] args) throws IOException {
        System.out.println(HELP);
        return 0;
    }

    public Integer exit(String[] args) throws IOException {
        this.exit = true;
        return 0;
    }

    public Integer handleInvalidCommand(String[] args) {
        System.out.println("Invalid command '" + args[0] + "'. Try 'help'");
        return 0;
    }
}

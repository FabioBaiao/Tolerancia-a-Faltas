package replication;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Rep;
import spread.SpreadGroup;
import tasks.TaskScheduler;

public class RepState extends Rep {

    private TaskScheduler taskScheduler;
    private String leader;

    public RepState() {}

    public RepState(TaskScheduler taskScheduler, String leader) {
        this.taskScheduler = taskScheduler;
        this.leader = leader;
    }

    public TaskScheduler getTaskScheduler() {
        return taskScheduler;
    }

    public String getLeader() {
        return leader;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        serializer.writeObject(taskScheduler, bufferOutput);
        bufferOutput.writeString(leader);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        this.taskScheduler = serializer.readObject(bufferInput);
        this.leader = bufferInput.readString();
    }
}

package replication;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Rep;
import spread.MembershipInfo;
import spread.SpreadGroup;
import tasks.TaskScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RepState extends Rep {

    private TaskScheduler taskScheduler;
    private String leader;
    private List<String> disconnClients;

    public RepState() {}

    public RepState(TaskScheduler taskScheduler, String leader, List<String> disconnClients) {
        this.taskScheduler = taskScheduler;
        this.leader = leader;
        this.disconnClients = disconnClients;
    }

    public TaskScheduler getTaskScheduler() {
        return taskScheduler;
    }

    public String getLeader() {
        return leader;
    }

    public List<String> getInfos() {
        return disconnClients;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        serializer.writeObject(taskScheduler, bufferOutput);
        bufferOutput.writeString(leader);

        bufferOutput.writeInt(disconnClients.size());
        for (String disconnCli : disconnClients)
            bufferOutput.writeString(disconnCli);

    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        this.taskScheduler = serializer.readObject(bufferInput);
        this.leader = bufferInput.readString();

        int size = bufferInput.readInt();
        this.disconnClients = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            this.disconnClients.add(bufferInput.readString());

    }
}

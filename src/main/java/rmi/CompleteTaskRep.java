package rmi;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;
import tasks.Task;

import java.util.Optional;

public class CompleteTaskRep extends Rep {

    private Optional<Task> maybeTask;

    public CompleteTaskRep() {
        super();
    }

    public CompleteTaskRep(int id, Optional<Task> maybeTask) {
        super(id);
        this.maybeTask = maybeTask;
    }

    public Optional<Task> getTask() {
        return maybeTask;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        super.writeObject(bufferOutput, serializer);
        serializer.writeObject(maybeTask, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        super.readObject(bufferInput, serializer);
        this.maybeTask = serializer.readObject(bufferInput);
    }
}

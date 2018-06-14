package rmi;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;
import tasks.Task;

import java.util.Optional;

public class CompleteTaskRep extends Rep {

    private Optional<Task> maybeTask;

    public CompleteTaskRep() {
    }

    public CompleteTaskRep(Optional<Task> maybeTask) {
        this.maybeTask = maybeTask;
    }

    public Optional<Task> getTask() {
        return maybeTask;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        serializer.writeObject(maybeTask, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        this.maybeTask = serializer.readObject(bufferInput);
    }
}

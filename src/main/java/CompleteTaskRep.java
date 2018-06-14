import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

import java.util.Optional;

public class CompleteTaskRep extends Rep {

    private Optional<Task> ot;

    public CompleteTaskRep() {}

    public CompleteTaskRep(Optional<Task> ot) {
        this.ot = ot;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {

    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {

    }
}

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

public class AddTaskReq extends Req {

    private String name;
    private String description;

    public AddTaskReq() {}

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeString(name);
        bufferOutput.writeString(description);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        name = bufferInput.readString();
        description = bufferInput.readString();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}

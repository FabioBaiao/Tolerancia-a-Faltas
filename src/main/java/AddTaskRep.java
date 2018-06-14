import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

public class AddTaskRep extends Rep {

    private String url;

    public AddTaskRep() {}

    public AddTaskRep(String url) {
        this.url = url;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeString(url);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        url = bufferInput.readString();
    }
}

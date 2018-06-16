package rmi;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

public class AssignTaskReq extends Req {

    private String username;

    public AssignTaskReq() {
    }

    public AssignTaskReq(int id, String username) {
        super(id);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        super.writeObject(bufferOutput, serializer);
        bufferOutput.writeString(username);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        super.readObject(bufferInput, serializer);
        this.username = bufferInput.readString();
    }
}

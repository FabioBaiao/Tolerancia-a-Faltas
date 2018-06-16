package rmi;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

public class AssignTaskReq extends Req {

    private String privateGroupName;

    public AssignTaskReq() {
    }

    public AssignTaskReq(int id, String privateGroupName) {
        super(id);
        this.privateGroupName = privateGroupName;
    }

    public String getPrivateGroupName() {
        return privateGroupName;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        super.writeObject(bufferOutput, serializer);
        bufferOutput.writeString(privateGroupName);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        super.readObject(bufferInput, serializer);
        this.privateGroupName = bufferInput.readString();
    }
}

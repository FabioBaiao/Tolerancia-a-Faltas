package rmi;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

import java.time.LocalDateTime;

public class CompleteTaskReq extends Req {

    private String privateGroupName;
    private String url;
    private LocalDateTime completionDateTime;

    public CompleteTaskReq() {
        super();
    }

    public CompleteTaskReq(int id, String privateGroupName, String url) {
        this(id, privateGroupName, url, LocalDateTime.now());
    }

    public CompleteTaskReq(int id, String privateGroupName, String url, LocalDateTime completionDateTime) {
        super(id);
        this.privateGroupName = privateGroupName;
        this.url = url;
        this.completionDateTime = completionDateTime;
    }

    public String getPrivateGroupName() {
        return privateGroupName;
    }

    public String getUrl() {
        return url;
    }

    public LocalDateTime getCompletionDateTime() {
        return completionDateTime;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        super.writeObject(bufferOutput, serializer);
        bufferOutput.writeString(privateGroupName);
        bufferOutput.writeString(url);
        serializer.writeObject(completionDateTime, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        super.readObject(bufferInput, serializer);
        this.privateGroupName = bufferInput.readString();
        this.url = bufferInput.readString();
        this.completionDateTime = serializer.readObject(bufferInput);
    }
}

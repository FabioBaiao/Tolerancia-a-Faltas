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
    }

    public CompleteTaskReq(String privateGroupName, String url) {
        this(privateGroupName, url, LocalDateTime.now());
    }

    public CompleteTaskReq(String privateGroupName, String url, LocalDateTime completionDateTime) {
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
        bufferOutput.writeString(privateGroupName);
        bufferOutput.writeString(url);
        serializer.writeObject(completionDateTime, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        this.privateGroupName = bufferInput.readString();
        this.url = bufferInput.readString();
        this.completionDateTime = serializer.readObject(bufferInput);
    }
}

package rmi;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

import java.time.LocalDateTime;

public class CompleteTaskReq extends Req {

    private String username;
    private String url;
    private LocalDateTime completionDateTime;

    public CompleteTaskReq() {
        super();
    }

    public CompleteTaskReq(int id, String username, String url) {
        this(id, username, url, LocalDateTime.now());
    }

    public CompleteTaskReq(int id, String username, String url, LocalDateTime completionDateTime) {
        super(id);
        this.username = username;
        this.url = url;
        this.completionDateTime = completionDateTime;
    }

    public String getUsername() {
        return username;
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
        bufferOutput.writeString(username);
        bufferOutput.writeString(url);
        serializer.writeObject(completionDateTime, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        super.readObject(bufferInput, serializer);
        this.username = bufferInput.readString();
        this.url = bufferInput.readString();
        this.completionDateTime = serializer.readObject(bufferInput);
    }
}

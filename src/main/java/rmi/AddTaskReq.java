package rmi;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;

import java.time.LocalDateTime;

public class AddTaskReq extends Req {

    private String name;
    private String description;
    private LocalDateTime creationDateTime;

    public AddTaskReq() {
    }

    public AddTaskReq(String name, String description) {
        this(name, description, LocalDateTime.now());
    }

    public AddTaskReq(String name, String description, LocalDateTime creationDateTime) {
        this.name = name;
        this.description = description;
        this.creationDateTime = creationDateTime;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreationDateTime() {
        return creationDateTime;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeString(name);
        bufferOutput.writeString(description);
        serializer.writeObject(creationDateTime, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        this.name = bufferInput.readString();
        this.description = bufferInput.readString();
        this.creationDateTime = serializer.readObject(bufferInput);
    }
}

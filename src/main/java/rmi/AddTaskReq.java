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
        super();
    }

    public AddTaskReq(int id, String name, String description) {
        this(id, name, description, LocalDateTime.now());
    }

    public AddTaskReq(int id, String name, String description, LocalDateTime creationDateTime) {
        super(id);
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
        super.writeObject(bufferOutput, serializer);
        bufferOutput.writeString(name);
        bufferOutput.writeString(description);
        serializer.writeObject(creationDateTime, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        super.readObject(bufferInput, serializer);
        this.name = bufferInput.readString();
        this.description = bufferInput.readString();
        this.creationDateTime = serializer.readObject(bufferInput);
    }
}

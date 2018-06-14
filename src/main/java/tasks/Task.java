package tasks;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class Task implements CatalystSerializable, Comparable<Task> {

    private long id;
    private String url;
    private String name;
    private String description;
    private LocalDateTime creationDateTime;
    private LocalDateTime completionDateTime;

    public Task() {
    }

    public Task(long id, String url, String name, String description, LocalDateTime creationDateTime) {
        this.id = id;
        this.url = url;
        this.name = name;
        this.description = description;
        this.creationDateTime = creationDateTime;
        this.completionDateTime = null;
    }

    public long getId() {
        return id;
    }

    public String getUrl() {
        return url;
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

    public Optional<LocalDateTime> getCompletionDateTime() {
        return Optional.ofNullable(completionDateTime);
    }

    /**
     * @return <tt>true</tt> if this tasks.Task wasn't already complete; <tt>false</tt> otherwise
     */
    public boolean complete() {
        if (completionDateTime != null) {
            return false;
        }
        completionDateTime = LocalDateTime.now();

        return true;
    }

    public boolean complete(LocalDateTime completionDateTime) {
        if (this.completionDateTime != null) {
            return false;
        }

        if (!completionDateTime.isAfter(creationDateTime)) {
            throw new IllegalArgumentException("Completion date and time must be after creation date and time");
        }
        this.completionDateTime = completionDateTime;

        return true;
    }

    public boolean isComplete() {
        return completionDateTime != null;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeLong(id);
        bufferOutput.writeString(url);
        bufferOutput.writeString(name);
        bufferOutput.writeString(description);

        serializer.writeObject(creationDateTime, bufferOutput);

        boolean isComplete = completionDateTime != null;
        bufferOutput.writeBoolean(isComplete);

        if (isComplete) {
            serializer.writeObject(completionDateTime, bufferOutput);
        }
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        this.id = bufferInput.readLong();
        this.url = bufferInput.readString();
        this.name = bufferInput.readString();
        this.description = bufferInput.readString();

        this.completionDateTime = serializer.readObject(bufferInput);

        boolean isComplete = bufferInput.readBoolean();
        if (isComplete) {
            this.completionDateTime = serializer.readObject(bufferInput);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof Task))
            return false;

        Task other = (Task) obj;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "tasks.Task{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", creationDateTime=" + creationDateTime +
                ", completionDateTime=" + completionDateTime +
                '}';
    }

    @Override
    public int compareTo(Task other) {
        return Long.compare(this.id, other.id);
    }
}

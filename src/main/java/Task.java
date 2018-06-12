import converters.LocalDateTimeConverter;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class Task implements CatalystSerializable, Comparable<Task> {

    private String url;
    private String name;
    private String description;
    private LocalDateTime creationDateTime;
    private LocalDateTime completionDateTime;

    public Task() {
    }

    public Task(String url, String name, String description) {
        this.url = url;
        this.name = name;
        this.description = description;
        this.creationDateTime = LocalDateTime.now();
        this.completionDateTime = null;
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
     * @return <tt>true</tt> if this Task wasn't already complete; <tt>false</tt> otherwise
     */
    public boolean complete() {
        if (completionDateTime != null)
            return false;

        completionDateTime = LocalDateTime.now();
        return true;
    }

    public boolean isComplete() {
        return completionDateTime != null;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeString(url);
        bufferOutput.writeString(name);
        bufferOutput.writeString(description);

        long creationTimestamp = LocalDateTimeConverter.convertToTimestamp(creationDateTime);
        bufferOutput.writeLong(creationTimestamp);

        boolean isComplete = completionDateTime != null;
        bufferOutput.writeBoolean(isComplete);

        if (isComplete) {
            long completionTimestamp = LocalDateTimeConverter.convertToTimestamp(completionDateTime);
            bufferOutput.writeLong(completionTimestamp);
        }
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        this.url = bufferInput.readString();
        this.name = bufferInput.readString();
        this.description = bufferInput.readString();

        long creationTimestamp = bufferInput.readLong();
        this.creationDateTime = LocalDateTimeConverter.convertFromTimestamp(creationTimestamp);

        boolean isComplete = bufferInput.readBoolean();
        if (isComplete) {
            long completionTimestamp = bufferInput.readLong();
            this.completionDateTime = LocalDateTimeConverter.convertFromTimestamp(completionTimestamp);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof Task))
            return false;

        Task task = (Task) obj;
        return Objects.equals(url, task.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public String toString() {
        return "Task{" +
                "url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", creationDateTime=" + creationDateTime +
                ", completionDateTime=" + completionDateTime +
                '}';
    }

    @Override
    public int compareTo(Task other) {
        return this.url.compareTo(other.url);
    }
}

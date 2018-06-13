package serializers.time;

import converters.LocalDateTimeConverter;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.serializer.TypeSerializer;

import java.time.LocalDateTime;

public class LocalDateTimeSerializer implements TypeSerializer<LocalDateTime> {

    @Override
    public void write(LocalDateTime dateTime, BufferOutput bufferOutput, Serializer serializer) {
        bufferOutput.writeLong(LocalDateTimeConverter.convertToTimestamp(dateTime));
    }

    @Override
    public LocalDateTime read(Class<LocalDateTime> type, BufferInput bufferInput, Serializer serializer) {
        return LocalDateTimeConverter.convertFromTimestamp(bufferInput.readLong());
    }
}

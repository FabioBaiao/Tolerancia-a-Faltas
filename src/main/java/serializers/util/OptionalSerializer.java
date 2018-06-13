package serializers.util;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.serializer.TypeSerializer;

import java.util.Optional;

public class OptionalSerializer implements TypeSerializer<Optional> {

    @Override
    public void write(Optional optional, BufferOutput bufferOutput, Serializer serializer) {
        final boolean isPresent = optional.isPresent();

        bufferOutput.writeBoolean(isPresent);
        if (isPresent) {
            serializer.writeObject(optional.get(), bufferOutput);
        }
    }

    @Override
    public Optional read(Class<Optional> type, BufferInput bufferInput, Serializer serializer) {
        final boolean isPresent = bufferInput.readBoolean();

        return isPresent ? Optional.of(serializer.readObject(bufferInput)) : Optional.empty();
    }
}

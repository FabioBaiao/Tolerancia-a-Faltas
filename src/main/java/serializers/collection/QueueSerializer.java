package serializers.collection;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.serializer.TypeSerializer;

import java.util.Queue;

public abstract class QueueSerializer<T extends Queue> implements TypeSerializer<T> {

    protected abstract T createQueue(int size);

    @Override
    public void write(T queue, BufferOutput bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(queue.size());
        for (Object o : queue) {
            serializer.writeObject(o, bufferOutput);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T read(Class<T> type, BufferInput bufferInput, Serializer serializer) {
        final int size = bufferInput.readInt();
        T queue = createQueue(size);

        for (int i = 0; i < size; i++) {
            queue.add(serializer.readObject(bufferInput));
        }
        return queue;
    }
}

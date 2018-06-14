package serializers.collection;

import java.util.PriorityQueue;

public class PriorityQueueSerializer extends QueueSerializer<PriorityQueue<?>> {

    @Override
    protected PriorityQueue<?> createQueue(int size) {
        return (size <= 0 ? new PriorityQueue<>() : new PriorityQueue<>(size));
    }
}

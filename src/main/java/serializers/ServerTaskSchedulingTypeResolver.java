package serializers;

import io.atomix.catalyst.serializer.SerializerRegistry;
import serializers.collection.PriorityQueueSerializer;

import java.util.PriorityQueue;

public class ServerTaskSchedulingTypeResolver extends BaseTaskSchedulingTypeResolver {

    @Override
    public void resolve(SerializerRegistry serializerRegistry) {
        super.resolve(serializerRegistry);
        serializerRegistry.registerAbstract(PriorityQueue.class, PriorityQueueSerializer.class);
    }
}

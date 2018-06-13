package serializers;

import io.atomix.catalyst.serializer.SerializableTypeResolver;
import io.atomix.catalyst.serializer.SerializerRegistry;
import serializers.collection.PriorityQueueSerializer;
import serializers.time.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.Deque;

// TODO: Move seriazible types to packages
public class TaskSchedulingTypeResolver implements SerializableTypeResolver {

    @Override
    public void resolve(SerializerRegistry serializerRegistry) {
        serializerRegistry.registerAbstract(Deque.class, PriorityQueueSerializer.class);
        serializerRegistry.register(LocalDateTime.class, LocalDateTimeSerializer.class);
        // serializerRegistry.register(ReqState.class);
        // serializerRegistry.register(RepState.class);
        // serializerRegistry.register();
    }
}

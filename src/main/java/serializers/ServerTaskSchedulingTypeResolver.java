package serializers;

import io.atomix.catalyst.serializer.SerializableTypeResolver;
import io.atomix.catalyst.serializer.SerializerRegistry;
import replication.RepState;
import replication.ReqState;
import rmi.*;
import serializers.collection.PriorityQueueSerializer;
import serializers.time.LocalDateTimeSerializer;
import serializers.util.OptionalSerializer;
import tasks.Task;

import java.time.LocalDateTime;
import java.util.Deque;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;

public class ServerTaskSchedulingTypeResolver extends BaseTaskSchedulingTypeResolver {

    @Override
    public void resolve(SerializerRegistry serializerRegistry) {
        super.resolve(serializerRegistry);
        serializerRegistry.registerAbstract(PriorityQueue.class, PriorityQueueSerializer.class);
    }
}

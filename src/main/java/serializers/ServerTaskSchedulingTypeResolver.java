package serializers;

import io.atomix.catalyst.serializer.SerializerRegistry;
import replication.AckState;
import replication.RepState;
import rmi.UnassignAll;
import serializers.collection.PriorityQueueSerializer;

import java.util.PriorityQueue;

public class ServerTaskSchedulingTypeResolver extends BaseTaskSchedulingTypeResolver {

    @Override
    public void resolve(SerializerRegistry serializerRegistry) {
        super.resolve(serializerRegistry);
        serializerRegistry.register(PriorityQueue.class, PriorityQueueSerializer.class);
        serializerRegistry.register(RepState.class);
        serializerRegistry.register(AckState.class);
        serializerRegistry.register(UnassignAll.class);
    }
}

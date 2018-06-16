package serializers;

import io.atomix.catalyst.serializer.SerializableTypeResolver;
import io.atomix.catalyst.serializer.SerializerRegistry;
import rmi.*;
import serializers.time.LocalDateTimeSerializer;
import serializers.util.OptionalSerializer;
import tasks.Task;

import java.time.LocalDateTime;
import java.util.Optional;

public class BaseTaskSchedulingTypeResolver implements SerializableTypeResolver {
    @Override
    public void resolve(SerializerRegistry serializerRegistry) {
        serializerRegistry.register(Optional.class, OptionalSerializer.class);
        serializerRegistry.register(LocalDateTime.class, LocalDateTimeSerializer.class);

        serializerRegistry.register(Task.class);

        serializerRegistry.register(AddTaskReq.class);
        serializerRegistry.register(AddTaskRep.class);
        serializerRegistry.register(AssignTaskReq.class);
        serializerRegistry.register(AssignTaskRep.class);
        serializerRegistry.register(CompleteTaskReq.class);
        serializerRegistry.register(CompleteTaskRep.class);
    }
}

package replication;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Rep;

public class RepState extends Rep {

    private State state;

    public RepState() {}

    public RepState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        serializer.writeObject(state, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        this.state = serializer.readObject(bufferInput);
    }
}

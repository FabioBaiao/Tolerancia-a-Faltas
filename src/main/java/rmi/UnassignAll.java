package rmi;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import pt.haslab.ekit.Spread;
import spread.SpreadGroup;

public class UnassignAll implements CatalystSerializable {

    private String disconnected;

    public UnassignAll() {}

    public UnassignAll(String disconnected) {
        this.disconnected = disconnected;
    }

    public String getDisconnected() {
        return disconnected;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeString(disconnected);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        disconnected = bufferInput.readString();
    }
}

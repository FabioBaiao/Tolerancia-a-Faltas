import spread.SpreadMessage;

public class Tuple<T> {
    private Class<T> type;
    private SpreadMessage msg;
    private T object;

    public Tuple(Class<T> type, SpreadMessage msg, T object) {
        this.type = type;
        this.msg = msg;
        this.object = object;
    }

    public Class<T> getType() {
        return type;
    }

    public SpreadMessage getMsg() {
        return msg;
    }

    public T getObject() {
        return object;
    }
}

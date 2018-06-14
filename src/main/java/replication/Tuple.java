package replication;

import spread.SpreadMessage;

public class Tuple<T> {
    private Class<T> type;
    private SpreadMessage msg;
    private T req;

    public Tuple(Class<T> type, SpreadMessage msg, T req) {
        this.type = type;
        this.msg = msg;
        this.req = req;
    }

    public Class<T> getType() {
        return type;
    }

    public SpreadMessage getMsg() {
        return msg;
    }

    public T getReq() {
        return req;
    }
}

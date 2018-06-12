import spread.SpreadMessage;

public class Tuple<T> {
    private Class<T> type;
    private SpreadMessage msg;
    private Req req;

    public Tuple(Class<T> type, SpreadMessage msg, Req req) {
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

    public Req getReq() {
        return req;
    }
}

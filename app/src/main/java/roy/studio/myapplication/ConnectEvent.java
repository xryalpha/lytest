package roy.studio.myapplication;

public class ConnectEvent {
    public final String msg;
    public final int state;
    public ConnectEvent(int state,String msg) {
        this.state=state;
        this.msg = msg;
    }
}

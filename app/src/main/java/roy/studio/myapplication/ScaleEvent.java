package roy.studio.myapplication;

public class ScaleEvent {
    public final String callback;
    public final int config;
    public ScaleEvent(int config,String callback) {
        this.config=config;
        this.callback = callback;
    }
}

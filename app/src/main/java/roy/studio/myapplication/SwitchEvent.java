package roy.studio.myapplication;

public class SwitchEvent {
    public final Boolean sw;
    public final int config;
    public SwitchEvent(int config,Boolean sw) {
        this.config=config;
        this.sw = sw;
    }
}


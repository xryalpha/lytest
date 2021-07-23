package roy.studio.myapplication;

import com.google.gson.Gson;
public class GsonA{

    /**
     * name : 222
     * mac : 333
     * pin : 666
     */

    private String name;
    private String mac;
    private String pin;

    public static GsonA objectFromData(String str) {

        return new Gson().fromJson(str, GsonA.class);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}

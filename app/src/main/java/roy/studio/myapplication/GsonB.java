package roy.studio.myapplication;

import com.google.gson.Gson;

import java.io.Serializable;

public class GsonB implements Serializable {

    /**
     * t : 1
     * p : 1
     * v : 1
     */

    private String t;
    private String p;
    private String v;

    public static GsonB objectFromData(String str) {

        return new Gson().fromJson(str, GsonB.class);
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }
}

package se.tink.backend.integration.boot.configuration;

public class Configuration {
    String doX = "default value";
    String doY;

    public String getDoX() {
        return doX;
    }

    public void setDoX(String doX) {
        this.doX = doX;
    }

    public String getDoY() {
        return doY;
    }

    public void setDoY(String doY) {
        this.doY = doY;
    }
}

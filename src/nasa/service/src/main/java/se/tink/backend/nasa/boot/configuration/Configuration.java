package se.tink.backend.nasa.boot.configuration;

public class Configuration {
    private int port;
    private String keyStorePath;
    private String certAlias;
    private String trustStorePath;
    private boolean validateCerts;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public String getCertAlias() {
        return certAlias;
    }

    public void setCertAlias(String certAlias) {
        this.certAlias = certAlias;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    public boolean isValidateCerts() {
        return validateCerts;
    }

    public void setValidateCerts(boolean validateCerts) {
        this.validateCerts = validateCerts;
    }
}

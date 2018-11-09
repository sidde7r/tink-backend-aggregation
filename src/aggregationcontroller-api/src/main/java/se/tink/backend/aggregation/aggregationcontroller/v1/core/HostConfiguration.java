package se.tink.backend.aggregation.aggregationcontroller.v1.core;

import java.util.Base64;

public class HostConfiguration {
    private String clusterId;
    private String host;
    private String apiToken;
    private String base64encodedclientcert;
    private boolean disablerequestcompression;

    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    private HostConfiguration(String clusterId, String host, String apiToken, String base64encodedclientcert,
                              boolean disablerequestcompression) {
        this.clusterId = clusterId;
        this.host = host;
        this.apiToken = apiToken;
        this.base64encodedclientcert = base64encodedclientcert;
        this.disablerequestcompression = disablerequestcompression;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getBase64encodedclientcert() {
        return base64encodedclientcert;
    }

    public void setBase64encodedclientcert(String base64encodedclientcert) {
        this.base64encodedclientcert = base64encodedclientcert;
    }

    public boolean isDisablerequestcompression() {
        return disablerequestcompression;
    }

    public void setDisablerequestcompression(boolean disablerequestcompression) {
        this.disablerequestcompression = disablerequestcompression;
    }

    public byte[] getClientCert() {
        return BASE64_DECODER.decode(base64encodedclientcert);
    }


    public static HostConfiguration createForTesting( String clusterId) {
        return new HostConfiguration(clusterId, null, null,
                null, false);
    }

    public static HostConfiguration create(String clusterId, String host, String apiToken,
                                           String base64encodedclientcert, boolean disablerequestcompression) {
        return new HostConfiguration(clusterId, host, apiToken, base64encodedclientcert, disablerequestcompression);
    }
}

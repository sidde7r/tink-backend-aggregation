package se.tink.backend.aggregation.agents.contexts;

public interface EidasContext {

    String getAppId();

    void setAppId(String appId);

    String getClusterId();

    void setClusterId(String clusterId);

    String getCertId();

    void setCertId(String certId);
}

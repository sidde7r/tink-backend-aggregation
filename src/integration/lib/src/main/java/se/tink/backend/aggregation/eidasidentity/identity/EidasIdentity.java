package se.tink.backend.aggregation.eidasidentity.identity;

public class EidasIdentity {
    private final String clusterId;
    private final String appId;
    private final String certId;

    private final String requester;

    public EidasIdentity(String clusterId, String appId, String certId, Class<?> requester) {
        this.clusterId = clusterId;
        this.appId = appId;
        this.certId = certId;
        this.requester = requester.getCanonicalName();
    }

    public EidasIdentity(String clusterId, String appId, String certId, String requester) {
        this.clusterId = clusterId;
        this.appId = appId;
        this.certId = certId;
        this.requester = requester;
    }

    public String getAppId() {
        return appId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public String getRequester() {
        return requester;
    }

    public String getCertId() {
        return certId;
    }

    @Override
    public String toString() {
        return String.format(
                "{clusterId='%s', appId='%s', certId='%s', requester='%s'}",
                clusterId, appId, certId, requester);
    }
}

package se.tink.backend.aggregation.eidassigner.identity;

public class EidasIdentity {
    private final String clusterId;
    private final String appId;
    private final String certId;

    private final String requester;

    private static final String DEFAULT_CERT_ID = "DEFAULT";

    public EidasIdentity(String clusterId, String appId, Class requester) {
        this.clusterId = clusterId;
        this.appId = appId;
        this.certId = DEFAULT_CERT_ID;
        this.requester = requester.getCanonicalName();
    }

    public EidasIdentity(String clusterId, String appId, String requester) {
        this.clusterId = clusterId;
        this.appId = appId;
        this.certId = DEFAULT_CERT_ID;
        this.requester = requester;
    }

    public EidasIdentity(String clusterId, String appId, String certId, Class requester) {
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

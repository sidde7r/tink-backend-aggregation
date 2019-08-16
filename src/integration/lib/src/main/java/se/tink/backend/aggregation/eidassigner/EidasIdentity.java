package se.tink.backend.aggregation.eidassigner;

public class EidasIdentity {
    private final String clusterId;
    private final String appId;

    private final String signRequester;

    public EidasIdentity(String clusterId, String appId, Class signRequester) {
        this.clusterId = clusterId;
        this.appId = appId;

        this.signRequester = signRequester.getCanonicalName();
    }

    public EidasIdentity(String clusterId, String appId, String signRequester) {
        this.clusterId = clusterId;
        this.appId = appId;
        this.signRequester = signRequester;
    }

    public String getAppId() {
        return appId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public String getSignRequester() {
        return signRequester;
    }
}

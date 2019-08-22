package se.tink.backend.aggregation.eidassigner;

public class EidasIdentity {
    private final String clusterId;
    private final String appId;

    private final String requester;

    public EidasIdentity(String clusterId, String appId, Class requester) {
        this.clusterId = clusterId;
        this.appId = appId;

        this.requester = requester.getCanonicalName();
    }

    public EidasIdentity(String clusterId, String appId, String requester) {
        this.clusterId = clusterId;
        this.appId = appId;
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
}

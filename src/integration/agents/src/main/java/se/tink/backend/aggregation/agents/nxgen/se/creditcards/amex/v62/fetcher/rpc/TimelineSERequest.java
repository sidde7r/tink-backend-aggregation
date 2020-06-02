package se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex.v62.fetcher.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TimelineSERequest extends TimelineRequest {
    private BiometricsEntity biometrics = new BiometricsEntity();
    private boolean cmlEnabled;
    private boolean goodsSvcOfferEnabled;
    private boolean payWithPointsEnabled;
    private boolean payYourWayEnabled;
    private boolean pushEnabled;

    public void setCmlEnabled(boolean cmlEnabled) {
        this.cmlEnabled = cmlEnabled;
    }

    public void setGoodsSvcOfferEnabled(boolean goodsSvcOfferEnabled) {
        this.goodsSvcOfferEnabled = goodsSvcOfferEnabled;
    }

    public void setPayWithPointsEnabled(boolean payWithPointsEnabled) {
        this.payWithPointsEnabled = payWithPointsEnabled;
    }

    public void setPayYourWayEnabled(boolean payYourWayEnabled) {
        this.payYourWayEnabled = payYourWayEnabled;
    }

    public void setPushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }

    @JsonObject
    public class BiometricsEntity {
        private String faceIdStatus = "NotCapable";
        private String fingerprintStatus = "NotCapable";
    }
}

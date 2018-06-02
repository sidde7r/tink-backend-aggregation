package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.userregistration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.Response;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PinSentryInitResponse extends Response {
    /*
    {
        "registeredServices": null,
        "op_msg": null,
        "op_status": "00000",
        "pinSentryChallenge": "21003634",
        "op_gsn": "636p-01-05",
        "isFeatureOnHighRisk": false,
        "cardDigits": null,
        "deviceRegistrationStatus": 0
    }
     */
    private String pinSentryChallenge;
    private String cardDigits;

    public String getPinSentryChallenge() {
        return pinSentryChallenge;
    }

    public String getCardDigits() {
        return cardDigits;
    }
}

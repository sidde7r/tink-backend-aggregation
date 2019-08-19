package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.ChosenScaMethodEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizePaymentResponse {
    @JsonProperty("_links")
    private LinksEntity links;

    private ChallengeDataEntity challengeData;
    private ChosenScaMethodEntity chosenScaMethod;
    private String scaStatus;

    @JsonIgnore
    public LinksEntity getLinks() {
        return links;
    }

    @JsonIgnore
    public ChosenScaMethodEntity getChosenScaMethod() {
        return chosenScaMethod;
    }
}

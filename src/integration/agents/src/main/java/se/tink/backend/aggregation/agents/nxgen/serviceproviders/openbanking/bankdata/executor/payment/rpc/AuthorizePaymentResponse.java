package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizePaymentResponse {

    private String scaStatus;
    private ScaMethodEntity chosenScaMethod;

    @JsonProperty("_links")
    private LinksEntity links;
}

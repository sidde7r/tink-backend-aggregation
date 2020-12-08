package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
public class PaymentAuthorisationResponse {
    private String scaStatus;

    @JsonProperty("_links")
    private LinksEntity links;

    public URL getAuthorizationUrl() {
        return new URL(links.getScaRedirect().getUrl());
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StartAuthorizationProcessResponse {
    private String scaStatus;

    @JsonProperty("_links")
    private LinksEntity links;

    public String getScaRedirectLink() {
        return Optional.ofNullable(links.getScaRedirectEntity())
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        ErrorMessages.PAYMENT_CANT_BE_SIGNED_ERROR));
    }
}

package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignOptionsResponse {

    @JsonProperty("bank_id_sign_redirect_url")
    private String bankIdSignRedirectUrl;

    @JsonProperty("autostart_token")
    private String autostartToken;

    public String getBankIdSignRedirectUrl() {
        return Optional.ofNullable(bankIdSignRedirectUrl)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        ErrorMessages.MISSING_PAYMENT_REDIRECT_INFO));
    }
}

package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class VerificationOnboardingResponse {

    @JsonProperty("body")
    private Body body;

    @Data
    @JsonObject
    @NoArgsConstructor
    public static class Body {
        private String registerToken;
        private boolean syncWalletRequired;
        private boolean onboardingRequired;

        @JsonProperty("conti")
        private List<AccountDetails> accountsDetails;

        @Data
        @JsonObject
        @NoArgsConstructor
        public static class AccountDetails {

            @JsonProperty("numeroConto")
            private String accountNumber;

            @JsonProperty("attivo")
            private boolean isActive;
        }
    }
}

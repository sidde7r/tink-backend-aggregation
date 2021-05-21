package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@Getter
public class AuthorizationRequest {

    private final String transactionFrom;
    private final String transactionTo;
    private final String expires;

    public static AuthorizationRequestBuilder builder() {
        return new AuthorizationRequestBuilder();
    }

    public static class AuthorizationRequestBuilder {
        private int daysOfTransactions;
        private int daysToExpire;

        public AuthorizationRequestBuilder daysOfTransactions(int daysOfTransactions) {
            this.daysOfTransactions = daysOfTransactions;
            return this;
        }

        public AuthorizationRequestBuilder daysToExpire(int daysToExpire) {
            this.daysToExpire = daysToExpire;
            return this;
        }

        public AuthorizationRequest build() {
            OffsetDateTime now = OffsetDateTime.now();
            String transactionFrom = now.minusDays(daysOfTransactions).toLocalDate().toString();
            String transactionTo = now.toLocalDate().toString();
            String expires = now.plusDays(daysToExpire).toString();
            return new AuthorizationRequest(transactionFrom, transactionTo, expires);
        }
    }
}

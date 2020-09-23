package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentRequest {

    @JsonProperty("ValidUntil")
    private String validUntil;

    @JsonProperty("RecurringIndicator")
    private boolean recurringIndicator = true;

    @JsonProperty("Access")
    private AccessEntity access = new AccessEntity();

    @JsonProperty("FrequencyPerDay")
    private int frequencyPerDay = 4;

    @JsonProperty("CombinedServiceIndicator")
    private boolean combinedServiceIndicator = false;

    public ConsentRequest(String validUntil) {
        this.validUntil = validUntil;
    }

    @JsonObject
    private static class AccessEntity {
        @JsonProperty("Accounts")
        private List<String> accounts = Collections.emptyList();

        @JsonProperty("Balances")
        private List<String> balances = Collections.emptyList();

        @JsonProperty("Transactions")
        private List<String> transactions = Collections.emptyList();

        private String allPsd2 = "allAccounts";
    }
}

package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditorRequest {
    @JsonProperty("AccountNumber")
    private String accountNumber;

    @JsonProperty("CountryCode")
    private String countryCode;

    private CreditorRequest(String accountNumber, String countryCode) {
        this.accountNumber = accountNumber;
        this.countryCode = countryCode;
    }

    public static CreditorRequest create(String accountNumber, String countryCode) {
        return new CreditorRequest(accountNumber, countryCode);
    }
}

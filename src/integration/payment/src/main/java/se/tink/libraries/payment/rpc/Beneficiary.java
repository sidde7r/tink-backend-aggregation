package se.tink.libraries.payment.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import se.tink.libraries.account.AccountIdentifier;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Builder
public class Beneficiary {
    private final String name;
    private final String accountNumber;
    private final AccountIdentifier.Type accountNumberType;

    @JsonCreator
    public Beneficiary(
            @JsonProperty("name") String name,
            @JsonProperty("accountNumber") String accountNumber,
            @JsonProperty("accountNumberType") AccountIdentifier.Type accountNumberType) {
        this.name = name;
        this.accountNumber = accountNumber;
        this.accountNumberType = accountNumberType;
    }
}

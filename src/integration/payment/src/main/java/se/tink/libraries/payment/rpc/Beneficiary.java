package se.tink.libraries.payment.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import se.tink.libraries.account.enums.AccountIdentifierType;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
public class Beneficiary {
    private final String name;
    private final String accountNumber;
    private final AccountIdentifierType accountNumberType;

    @JsonCreator
    public Beneficiary(
            @JsonProperty("name") String name,
            @JsonProperty("accountNumber") String accountNumber,
            @JsonProperty("accountNumberType") AccountIdentifierType accountNumberType) {
        this.name = name;
        this.accountNumber = accountNumber;
        this.accountNumberType = accountNumberType;
    }
}

package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountEntity {
    private String name;
    private String accountNumber;
    private String clearingNumber;

    @JsonProperty("fullyFormatterNumber")
    private String fullyFormattedNumber;
}

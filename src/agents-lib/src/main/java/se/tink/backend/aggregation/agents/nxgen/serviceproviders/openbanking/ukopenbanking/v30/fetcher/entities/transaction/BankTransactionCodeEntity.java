package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.entities.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankTransactionCodeEntity {
    @JsonProperty("Code")
    private UkOpenBankingConstants.BankTransactionCode code;
    @JsonProperty("SubCode")
    private String subCode;

    public UkOpenBankingConstants.BankTransactionCode getCode() {
        return code;
    }
}

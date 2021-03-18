package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.libraries.account.enums.AccountIdentifierType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BankTransferListEntity extends TransferListEntity {
    @JsonProperty("BANK_PREFIX")
    public String BankPrefix;

    @JsonProperty("OVERF_DAT")
    public String TransferDate;

    @Override
    @JsonIgnore
    public String getTransferDateString() {
        return TransferDate;
    }

    @Override
    @JsonIgnore
    public AccountIdentifierType getDestinationType() {
        return AccountIdentifierType.SE;
    }
}

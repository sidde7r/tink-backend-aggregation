package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import se.tink.libraries.account.AccountIdentifier;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceTransferListEntity extends TransferListEntity {
    @JsonProperty("MOTT_KONTO_TYP")
    public String DestinationType;

    @JsonProperty("START_DATUM")
    public String TransferDate;

    @Override
    @JsonIgnore
    public String getTransferDateString() {
        return TransferDate;
    }

    @Override
    @JsonIgnore
    public AccountIdentifier.Type getDestinationType() {
        if (Objects.equal(DestinationType.trim(), "PG")) {
            return AccountIdentifier.Type.SE_PG;
        } else if (Objects.equal(DestinationType.trim(), "BG")) {
            return AccountIdentifier.Type.SE_BG;
        }

        return null;
    }
}

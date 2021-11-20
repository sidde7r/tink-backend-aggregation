package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.entity;

import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.EMPTY_STRING;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class NickelAccountDetails {
    private String accountNumber;
    private NickelAccountAgency agency;
    private String bankCode;
    private String bic;
    private String branchCode;
    private NickelAccountHolder holder;
    private String iban;
    private String key;

    @JsonIgnore
    public String getHolderName() {
        if (null != holder) {
            return holder.getName();
        }
        return EMPTY_STRING;
    }
}

package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;

@JsonObject
public class AccountEntity {
    private String alias;
    private AmountEntity amount;
    private String availability;
    private String bic;
    private String contractCode;
    private String contractNumberFormatted;
    private String description;
    private String entityCode;
    private String hashIban;
    private String iban;
    private boolean isIberSecurities;
    private boolean isOwner;
    private boolean isSBPManaged;
    private String joint;
    private String mobileWarning;
    private int numOwners;
    private String number;
    private String owner;
    private String product;
    private String productType;
    private String value;

    public String getContractNumberFormatted() {
        return contractNumberFormatted;
    }

    public String getDescription() {
        return description;
    }

    public String getOwner() {
        return owner;
    }

    @JsonIgnore
    public HolderName getHolder() {
        return new HolderName(getOwner());
    }

    public String getIban() {
        return iban;
    }

    public String getBic() {
        return bic.replace(" ", "");
    }
}

package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transferdestination.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class TransferCreditorIdentity implements GeneralAccountEntity {
    @JsonProperty("creditorUse")
    private List<CreditorUseItem> creditorUse;

    @JsonProperty("holderIndicator")
    private boolean holderIndicator;

    @JsonProperty("activationDate")
    private String activationDate;

    @JsonProperty("rib")
    private Rib rib;

    @JsonProperty("label")
    private String label;

    @JsonProperty("creditorType")
    private CreditorType creditorType;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("balance")
    private Balance balance;

    @JsonProperty("iban")
    private String iban;

    @JsonProperty("bankLabel")
    private String bankLabel;

    @JsonProperty("bic")
    private String bic;

    @JsonProperty("designationLabel")
    private String designationLabel;

    @JsonProperty("email")
    private String email;

    @JsonIgnore
    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return new IbanIdentifier(iban);
    }

    @JsonIgnore
    @Override
    public String generalGetBank() {
        return bankLabel;
    }

    @JsonIgnore
    @Override
    public String generalGetName() {
        return designationLabel;
    }

    @JsonIgnore
    public boolean isDestinationAccount() {
        return !isOwnAccount();
    }

    @JsonIgnore
    public boolean isOwnAccount() {
        return holderIndicator;
    }

    @JsonIgnore
    public boolean isActivated() {
        return Strings.isNullOrEmpty(activationDate);
    }
}

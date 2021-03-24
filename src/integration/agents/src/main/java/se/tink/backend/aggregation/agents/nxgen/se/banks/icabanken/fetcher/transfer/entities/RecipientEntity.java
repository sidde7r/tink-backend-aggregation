package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonObject
public class RecipientEntity implements GeneralAccountEntity {
    @JsonProperty("AccountNumber")
    private String accountNumber;

    @JsonProperty("BudgetGroup")
    private String budgetGroup;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("RecipientId")
    private String recipientId;

    @JsonProperty("TransferBankId")
    private String transferBankId;

    @JsonProperty("Type")
    private String type;

    public RecipientEntity() {}

    public String getBudgetGroup() {
        return budgetGroup;
    }

    public void setBudgetGroup(String budgetGroup) {
        this.budgetGroup = budgetGroup;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name =
                !Strings.isNullOrEmpty(name) && name.length() > 20 ? name.substring(0, 20) : name;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getTransferBankId() {
        return transferBankId;
    }

    public void setTransferBankId(String transferBankId) {
        this.transferBankId = transferBankId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /*
     * The methods below are for general purposes
     */

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        switch (getType().toLowerCase()) {
            case IcaBankenConstants.AccountTypes.PAYMENT_BG:
                return new BankGiroIdentifier(getAccountNumber());
            case IcaBankenConstants.AccountTypes.PAYMENT_PG:
                return new PlusGiroIdentifier(getAccountNumber());
            default:
                return new SwedishIdentifier(getAccountNumber());
        }
    }

    @Override
    public String generalGetBank() {
        if (generalGetAccountIdentifier().isValid()
                && generalGetAccountIdentifier().is(AccountIdentifierType.SE)) {
            return generalGetAccountIdentifier().to(SwedishIdentifier.class).getBankName();
        }
        return null;
    }

    @Override
    public String generalGetName() {
        return getName();
    }

    @JsonIgnore
    public boolean isOwnAccount() {
        return false;
    }

    @JsonIgnore
    public String getUnformattedAccountNumber() {
        return accountNumber.replaceAll("[ -]", "");
    }
}

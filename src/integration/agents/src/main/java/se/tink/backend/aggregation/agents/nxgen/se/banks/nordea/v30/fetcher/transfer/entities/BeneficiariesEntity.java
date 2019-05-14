package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transfer.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonObject
public class BeneficiariesEntity implements GeneralAccountEntity {
    @JsonProperty("beneficiary_id")
    private String beneficiaryId;

    @JsonProperty("payment_type")
    private String paymentType;

    @JsonProperty private String name;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("account_number_type")
    private String accountNumberType;

    @JsonProperty("bank_code")
    private String bankCode;

    @JsonProperty("bank_name")
    private String bankName;

    @JsonProperty private String bic;

    @JsonIgnore
    public boolean isPgOrBg() {
        return accountNumberType.equalsIgnoreCase(NordeaSEConstants.PaymentAccountTypes.BANKGIRO)
                || accountNumberType.equalsIgnoreCase(
                        NordeaSEConstants.PaymentAccountTypes.PLUSGIRO);
    }

    @JsonIgnore
    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        switch (accountNumberType.toUpperCase()) {
            case NordeaSEConstants.PaymentAccountTypes.BANKGIRO:
                return new BankGiroIdentifier(accountNumber);
            case NordeaSEConstants.PaymentAccountTypes.PLUSGIRO:
                return new PlusGiroIdentifier(accountNumber);
            default:
                return new SwedishIdentifier(accountNumber);
        }
    }

    @JsonIgnore
    @Override
    public String generalGetBank() {
        AccountIdentifier accountIdentifier = generalGetAccountIdentifier();
        return accountIdentifier.isValid() && accountIdentifier.is(AccountIdentifier.Type.SE)
                ? accountIdentifier.to(SwedishIdentifier.class).getBankName()
                : null;
    }

    @JsonIgnore
    @Override
    public String generalGetName() {
        return name;
    }

    @JsonIgnore
    public String getName() {
        return name;
    }

    @JsonIgnore
    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public String getAccountNumber() {
        return accountNumber;
    }

    @JsonIgnore
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @JsonIgnore
    public String getBankName() {
        return bankName;
    }

    @JsonIgnore
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
}

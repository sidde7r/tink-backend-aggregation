package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transfer.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.NDAPersonalNumberIdentifier;
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
        return accountNumberType.equalsIgnoreCase(NordeaBaseConstants.PaymentAccountTypes.BANKGIRO)
                || accountNumberType.equalsIgnoreCase(
                        NordeaBaseConstants.PaymentAccountTypes.PLUSGIRO);
    }

    @JsonIgnore
    public boolean isLBAN() {
        return NordeaBaseConstants.PaymentAccountTypes.LBAN.equalsIgnoreCase(accountNumberType)
                || NordeaBaseConstants.PaymentAccountTypes.NDASE.equalsIgnoreCase(
                        accountNumberType);
    }

    @JsonIgnore
    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        switch (accountNumberType.toUpperCase()) {
            case NordeaBaseConstants.PaymentAccountTypes.BANKGIRO:
                return new BankGiroIdentifier(accountNumber);
            case NordeaBaseConstants.PaymentAccountTypes.PLUSGIRO:
                return new PlusGiroIdentifier(accountNumber);
            default:
                NDAPersonalNumberIdentifier ssnIdentifier =
                        new NDAPersonalNumberIdentifier(accountNumber);
                if (ssnIdentifier.isValid()) {
                    return ssnIdentifier.toSwedishIdentifier();
                } else {
                    return new SwedishIdentifier(accountNumber);
                }
        }
    }

    @JsonIgnore
    @Override
    public String generalGetBank() {
        AccountIdentifier accountIdentifier = generalGetAccountIdentifier();
        if (accountIdentifier.isValid() && accountIdentifier.is(AccountIdentifierType.SE)) {
            return accountIdentifier.to(SwedishIdentifier.class).getBankName();
        }
        return null;
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

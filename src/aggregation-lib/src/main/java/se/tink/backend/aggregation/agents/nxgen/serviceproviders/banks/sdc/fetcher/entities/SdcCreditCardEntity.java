package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.utils.StringUtils;

@JsonObject
public class SdcCreditCardEntity {
    private SdcCreditCardEntityKey entityKey;
    private String creditcardType;
    private String creditcardTypeName;
    private String creditcardStatus;
    private String statusEffectiveDate;
    private String creditcardNumber;
    private String cardHolderName;
    private SdcCreditCardAccountEntity attachedAccount;
    private String endDate;
    private String creditcardStatusType;

    @JsonIgnore
    public boolean isCreditCard() {
        return creditcardTypeName != null
                && SdcConstants.Fetcher.CREDIT_CARD_NAME_TOKENS.contains(creditcardTypeName);
    }

    public SdcCreditCardEntityKey getEntityKey() {
        return entityKey;
    }

    public String getCreditcardType() {
        return creditcardType;
    }

    public String getCreditcardTypeName() {
        return creditcardTypeName;
    }

    public String getCreditcardStatus() {
        return creditcardStatus;
    }

    public String getStatusEffectiveDate() {
        return statusEffectiveDate;
    }

    public String getCreditcardNumber() {
        return creditcardNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public SdcCreditCardAccountEntity getAttachedAccount() {
        return attachedAccount != null ? attachedAccount : new SdcCreditCardAccountEntity();
    }

    public String getEndDate() {
        return endDate;
    }

    public String getCreditcardStatusType() {
        return creditcardStatusType;
    }

    @JsonIgnore
    public CreditCardAccount toTinkCard(SdcAccount creditCardAccount, String bankCode) {
        return CreditCardAccount.builder(creditcardNumber.replaceAll(" ", ""),
                creditCardAccount.getAmount().toTinkAmount(), creditCardAccount.getAvailableAmount().toTinkAmount())
                .setName(creditCardAccount.getName())
                .setUniqueIdentifier(constructUniqueIdentifier())
                // TODO: this is temporary just to be able to log credit card transaction fetching for "no-storebrand" = "9680"
                .addToTemporaryStorage("BANK_CODE", bankCode)
                .build();
    }

    String constructUniqueIdentifier() {
        String normalizedAccountNumber = attachedAccount.getEntityKey().getAccountId().replace(".","");
        if (StringUtils.trimToNull(normalizedAccountNumber) == null) {
            throw new IllegalStateException("No account number present");
        }

        return normalizedAccountNumber;
    }

    public boolean belongsTo(SdcAccount a) {
        return a.isAccount(getAttachedAccount());
    }
}

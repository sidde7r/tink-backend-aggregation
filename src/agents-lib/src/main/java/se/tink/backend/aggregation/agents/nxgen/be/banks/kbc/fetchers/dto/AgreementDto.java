package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class AgreementDto implements GeneralAccountEntity {
    private TypeValuePair agreementNo;
    private TypeValuePair structureCode;
    private TypeValuePair productType;
    private TypeValuePair productTypeNr;
    private TypeValuePair agreementType;
    private TypeValuePair agreementName;
    private TypeValuePair rubricName;
    private TypeValuePair balance;
    private TypeValuePair currency;
    private TypeValuePair counterValueBalance;
    private TypeValuePair counterValueCurrency;
    private TypeValuePair transactionsAvailable;
    private TypeValuePair showBalance;
    private TypeValuePair companyNo;
    private TypeValuePair roleCode;
    private TypeValuePair statusCode;
    private TypeValuePair isBusiness;
    private TypeValuePair isCompleted;
    private AgreementMakeUpDto agreementMakeUp;
    private TypeValuePair visibilityIndicator;
    private TypeValuePair principalAccountHolder;
    private TypeValuePair balanceIncludingReservations;
    private TypeValuePair balanceIncludinReservationAmountEur;
    private TypeValuePair reservationAmount;
    private TypeValuePair reservationIndicator;
    private TypeValuePair agreementStructuredMessage;

    public TypeValuePair getAgreementNo() {
        return agreementNo;
    }

    public TypeValuePair getStructureCode() {
        return structureCode;
    }

    public TypeValuePair getProductType() {
        return productType;
    }

    public TypeValuePair getProductTypeNr() {
        return productTypeNr;
    }

    public TypeValuePair getAgreementType() {
        return agreementType;
    }

    public TypeValuePair getAgreementName() {
        return agreementName;
    }

    public TypeValuePair getRubricName() {
        return rubricName;
    }

    public TypeValuePair getBalance() {
        return balance;
    }

    public TypeValuePair getCurrency() {
        return currency;
    }

    public TypeValuePair getCounterValueBalance() {
        return counterValueBalance;
    }

    public TypeValuePair getCounterValueCurrency() {
        return counterValueCurrency;
    }

    public TypeValuePair getTransactionsAvailable() {
        return transactionsAvailable;
    }

    public TypeValuePair getShowBalance() {
        return showBalance;
    }

    public TypeValuePair getCompanyNo() {
        return companyNo;
    }

    public TypeValuePair getRoleCode() {
        return roleCode;
    }

    public TypeValuePair getStatusCode() {
        return statusCode;
    }

    public TypeValuePair getIsBusiness() {
        return isBusiness;
    }

    public TypeValuePair getIsCompleted() {
        return isCompleted;
    }

    public AgreementMakeUpDto getAgreementMakeUp() {
        return agreementMakeUp;
    }

    public TypeValuePair getVisibilityIndicator() {
        return visibilityIndicator;
    }

    public TypeValuePair getPrincipalAccountHolder() {
        return principalAccountHolder;
    }

    public TypeValuePair getBalanceIncludingReservations() {
        return balanceIncludingReservations;
    }

    public TypeValuePair getBalanceIncludinReservationAmountEur() {
        return balanceIncludinReservationAmountEur;
    }

    public TypeValuePair getReservationAmount() {
        return reservationAmount;
    }

    public TypeValuePair getReservationIndicator() {
        return reservationIndicator;
    }

    public TypeValuePair getAgreementStructuredMessage() {
        return agreementStructuredMessage;
    }

    public Amount getAmount() {
        return new Amount(currency.getValue(), Double.valueOf(balance.getValue()));
    }

    public boolean isKnownAccountType() {
        return KbcConstants.ACCOUNT_TYPES.containsKey(productTypeNr.getValue());
    }

    public boolean isTransactionalAccount() {
        switch (getAccountType()) {
        case CHECKING:
        case SAVINGS:
            return true;
        default:
            return false;
        }
    }

    public AccountTypes getAccountType() {
        return KbcConstants.ACCOUNT_TYPES.getOrDefault(productTypeNr.getValue(), AccountTypes.OTHER);
    }

    public TransactionalAccount toTransactionalAccount() {
        return TransactionalAccount.builder(getAccountType(), agreementNo.getValue(), getAmount())
                .setAccountNumber(agreementNo.getValue())
                .setName(agreementMakeUp.getName().getValue())
                .setHolderName(new HolderName(agreementName.getValue()))
                .setBankIdentifier(agreementNo.getValue())
                .addIdentifier(generalGetAccountIdentifier())
                .build();
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return AccountIdentifier.create(AccountIdentifier.Type.BE,
                agreementNo.getValue(), agreementName.getValue());
    }

    @Override
    public String generalGetBank() {
        return "";
    }

    @Override
    public String generalGetName() {
        return agreementName.getValue();
    }
}

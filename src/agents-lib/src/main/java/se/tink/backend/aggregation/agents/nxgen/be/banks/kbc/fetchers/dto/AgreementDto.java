package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeEncValueTuple;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.enums.AccountFlag;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.account.AccountIdentifier;

import java.util.Collection;
import java.util.Collections;

@JsonObject
public class AgreementDto implements GeneralAccountEntity {
    private TypeEncValueTuple agreementNo;
    private TypeValuePair structureCode;
    private TypeValuePair productType;
    private TypeEncValueTuple productTypeNr;
    private TypeEncValueTuple agreementType;
    private TypeEncValueTuple agreementName;
    private TypeValuePair rubricName;
    private TypeEncValueTuple balance;
    private TypeEncValueTuple currency;
    private TypeValuePair counterValueBalance;
    private TypeValuePair counterValueCurrency;
    private TypeValuePair transactionsAvailable;
    private TypeValuePair showBalance;
    private TypeEncValueTuple companyNo;
    private TypeEncValueTuple roleCode;
    private TypeEncValueTuple statusCode;
    private TypeValuePair isBusiness;
    private TypeValuePair isCompleted;
    private AgreementMakeUpDto agreementMakeUp;
    private TypeValuePair visibilityIndicator;
    private TypeEncValueTuple principalAccountHolder;
    private TypeValuePair balanceIncludingReservations;
    private TypeValuePair balanceIncludingReservationAmountEur;
    private TypeValuePair reservationAmount;
    private TypeValuePair reservationIndicator;
    private TypeValuePair agreementStructuredMessage;
    private TypeValuePair ribbonStatusCode;
    private TypeValuePair color;
    private TypeValuePair statusDescription;
    private TypeValuePair acceptThirdPartyClause;
    private TypeValuePair manageAccountInsurance;
    private TypeValuePair settleSavingsAccount;

    public TypeEncValueTuple getAgreementNo() {
        return agreementNo;
    }

    public TypeValuePair getStructureCode() {
        return structureCode;
    }

    public TypeValuePair getProductType() {
        return productType;
    }

    public TypeEncValueTuple getProductTypeNr() {
        return productTypeNr;
    }

    public TypeEncValueTuple getAgreementType() {
        return agreementType;
    }

    public TypeEncValueTuple getAgreementName() {
        return agreementName;
    }

    public TypeValuePair getRubricName() {
        return rubricName;
    }

    public TypeEncValueTuple getBalance() {
        return balance;
    }

    public TypeEncValueTuple getCurrency() {
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

    public TypeEncValueTuple getCompanyNo() {
        return companyNo;
    }

    public TypeEncValueTuple getRoleCode() {
        return roleCode;
    }

    public TypeEncValueTuple getStatusCode() {
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

    public TypeEncValueTuple getPrincipalAccountHolder() {
        return principalAccountHolder;
    }

    public TypeValuePair getBalanceIncludingReservations() {
        return balanceIncludingReservations;
    }

    public TypeValuePair getBalanceIncludingReservationAmountEur() {
        return balanceIncludingReservationAmountEur;
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

    public TypeValuePair getRibbonStatusCode() {
        return ribbonStatusCode;
    }

    public TypeValuePair getColor() {
        return color;
    }

    public TypeValuePair getStatusDescription() {
        return statusDescription;
    }

    public TypeValuePair getAcceptThirdPartyClause() {
        return acceptThirdPartyClause;
    }

    public TypeValuePair getManageAccountInsurance() {
        return manageAccountInsurance;
    }

    public TypeValuePair getSettleSavingsAccount() {
        return settleSavingsAccount;
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
                .setName(agreementMakeUp.getName().getValue())
                .setHolderName(new HolderName(agreementName.getValue()))
                .setBankIdentifier(agreementNo.getValue())
                .setAccountNumber(agreementNo.getValue())
                .addIdentifier(generalGetAccountIdentifier())
                .addAccountFlags(getAccountFlags())
                .build();
    }

    private Collection<AccountFlag> getAccountFlags() {
        return getIsBusinessAccount() ? Collections.singletonList(AccountFlag.BUSINESS) : Collections.EMPTY_LIST;
    }

    private boolean getIsBusinessAccount() {
        return isBusiness != null && Boolean.valueOf(isBusiness.getValue());
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return AccountIdentifier.create(AccountIdentifier.Type.SEPA_EUR,
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

package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import java.beans.Transient;
import java.util.Optional;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeEncValueTuple;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

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

    public ExactCurrencyAmount getAmount() {
        return ExactCurrencyAmount.of(Double.valueOf(balance.getValue()), currency.getValue());
    }

    @Transient
    public Optional<TransactionalAccountType> getAccountType() {
        return KbcConstants.ACCOUNT_TYPE_MAPPER.translate(productTypeNr.getValue());
    }

    public Optional<TransactionalAccount> toTransactionalAccount() {
        TransactionalAccountType accountType =
                getAccountType().orElseThrow(IllegalArgumentException::new);

        TransactionalBuildStep builder =
                TransactionalAccount.nxBuilder()
                        .withType(accountType)
                        .withoutFlags()
                        .withBalance(BalanceModule.of(getAmount()))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(agreementNo.getValue())
                                        .withAccountNumber(agreementNo.getValue())
                                        .withAccountName(agreementMakeUp.getName().getValue())
                                        .addIdentifier(generalGetAccountIdentifier())
                                        .build())
                        .setApiIdentifier(agreementNo.getValue())
                        .addHolderName(agreementName.getValue());

        if (accountType == TransactionalAccountType.CHECKING) {
            builder.addAccountFlags(AccountFlag.PSD2_PAYMENT_ACCOUNT);
        }

        AccountFlag accountFlag = getAccountFlag();

        if (null != accountFlag) {
            builder.addAccountFlags(accountFlag);
        }

        return builder.build();
    }

    private AccountFlag getAccountFlag() {
        return isBusinessAccount() ? AccountFlag.BUSINESS : null;
    }

    private boolean isBusinessAccount() {
        return isBusiness != null && Boolean.parseBoolean(isBusiness.getValue());
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return AccountIdentifier.create(
                AccountIdentifierType.SEPA_EUR, agreementNo.getValue(), agreementName.getValue());
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

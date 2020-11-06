package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.entity;

import java.util.Map;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CardEntity {
    private int cardPlasticId;
    private String nameOnCard;
    private boolean mostRecent;
    private int id;
    private String maskedCardNumber;
    private String state;
    private String physicalCardState;
    private String expirationDate;

    public int getCardPlasticId() {
        return cardPlasticId;
    }

    public String getNameOnCard() {
        return nameOnCard;
    }

    public boolean isMostRecent() {
        return mostRecent;
    }

    public int getId() {
        return id;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public String getState() {
        return state;
    }

    public String getPhysicalCardState() {
        return physicalCardState;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public CreditCardAccount toTinkCreditCardAccount(
            Map<String, CardAccountEntity> accountsHashMap,
            CardContractEntity contract,
            SebKortConfiguration config) {
        final CardAccountEntity account = accountsHashMap.get(contract.getCardAccountId());

        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(maskedCardNumber)
                                .withBalance(getBalanceIfOwner(account, contract))
                                .withAvailableCredit(
                                        getAvailableCreditIfOwnerIfPresent(account, contract))
                                .withCardAlias(contract.getProductName())
                                .build())
                .withFlagsFrom(SebKortConstants.PROVIDER_PSD2_FLAG_MAPPER, config.getProviderCode())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(maskedCardNumber)
                                .withAccountNumber(maskedCardNumber)
                                .withAccountName(contract.getProductName())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.PAYMENT_CARD_NUMBER,
                                                maskedCardNumber))
                                .build())
                .addHolderName(nameOnCard)
                .setApiIdentifier(contract.getId())
                .putInTemporaryStorage(
                        SebKortConstants.StorageKey.CARD_ACCOUNT_ID, contract.getCardAccountId())
                .putInTemporaryStorage(
                        SebKortConstants.StorageKey.IS_ACCOUNT_OWNER, contract.isOwned())
                .build();
    }

    private ExactCurrencyAmount getAvailableCreditIfOwnerIfPresent(
            CardAccountEntity account, CardContractEntity cardContract) {

        // Some SEBKort providers do not supply card accounts. In that case we can't set
        // available credit. We also don't want to set available credit for sub cards.
        if (Objects.isNull(account)
                || Objects.isNull(account.getDisposableAmount())
                || !cardContract.isOwned()) {
            return ExactCurrencyAmount.zero(cardContract.getCurrencyCode());
        }

        return ExactCurrencyAmount.of(account.getDisposableAmount(), account.getCurrencyCode());
    }

    private ExactCurrencyAmount getBalanceIfOwner(
            CardAccountEntity account, CardContractEntity cardContract) {

        // Some SEBKort providers do not supply card accounts. In this case we have to use the
        // amount from the card contract. This amount is not the whole truth, it's the amount
        // generated from transactions on that specific card during current period.
        if (Objects.isNull(account)) {
            return ExactCurrencyAmount.of(
                    cardContract.getNonBilledAmount().negate(), cardContract.getCurrencyCode());
        }

        // The card account balance is global for the whole account. Only set it for the account
        // owner, and set 0 for the secondary cards. Then we don't summarize to more debt than the
        // user actually has.
        return cardContract.isOwned()
                ? ExactCurrencyAmount.of(
                        account.getCurrentBalance().negate(), account.getCurrencyCode())
                : ExactCurrencyAmount.zero(cardContract.getCurrencyCode());
    }
}

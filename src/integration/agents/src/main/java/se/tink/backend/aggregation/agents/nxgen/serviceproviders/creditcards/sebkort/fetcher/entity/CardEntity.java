package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.entity;

import java.util.Map;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.amount.Amount;

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
            Map<String, CardAccountEntity> accountsHashMap, CardContractEntity contract) {
        final CardAccountEntity account = accountsHashMap.get(contract.getCardAccountId());

        return CreditCardAccount.builder(getMaskedCardNumber())
                .setAccountNumber(getMaskedCardNumber())
                .setName(contract.getProductName())
                .setHolderName(new HolderName(getNameOnCard()))
                .setBalance(getBalanceForOwner(account, contract))
                .setAvailableCredit(getAvailableCreditForOwnerIfPresent(account, contract))
                .setBankIdentifier(contract.getId())
                .putInTemporaryStorage(
                        SebKortConstants.StorageKey.CARD_ACCOUNT_ID, contract.getCardAccountId())
                .putInTemporaryStorage(
                        SebKortConstants.StorageKey.IS_ACCOUNT_OWNER, contract.isOwned())
                .build();
    }

    private Amount getAvailableCreditForOwnerIfPresent(
            CardAccountEntity account, CardContractEntity cardContract) {
        if (Objects.isNull(account)) {
            return null;
        }

        if (!cardContract.isOwned()) {
            if (!cardContract.isOwned()) {
                return new Amount(cardContract.getCurrencyCode(), 0d);
            }
        }

        return new Amount(account.getCurrencyCode(), account.getDisposableAmount());
    }

    private Amount getBalanceForOwner(CardAccountEntity account, CardContractEntity cardContract) {

        // Some SEBKort providers do not supply card accounts. In this case we have to use the
        // amount from the card contract. This amount is not the whole truth, it's the amount
        // generated from transactions on that specific card during current period.
        if (Objects.isNull(account)) {
            return new Amount(cardContract.getCurrencyCode(), cardContract.getNonBilledAmount())
                    .negate();
        }

        // The card account balance is global for the whole account. Only set it for the account
        // owner, and set 0 for the secondary cards. Then we don't summarize to more debt than the
        // user actually has.
        if (!cardContract.isOwned()) {
            return new Amount(cardContract.getCurrencyCode(), 0d);
        }

        return new Amount(account.getCurrencyCode(), account.getCurrentBalance()).negate();
    }
}

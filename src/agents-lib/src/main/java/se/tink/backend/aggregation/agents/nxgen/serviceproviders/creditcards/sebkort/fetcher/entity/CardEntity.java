package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.entity;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.core.Amount;

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

    public CreditCardAccount toTinkAccount(
            UserEntity user,
            CardContractEntity contract,
            CardAccountEntity account,
            CardEntity card) {
        String currency = account.getCurrencyCode();

        return CreditCardAccount.builder(card.getMaskedCardNumber())
                .setAccountNumber(card.getMaskedCardNumber())
                .setName(contract.getProductName())
                .setBankIdentifier(account.getId())
                .putInTemporaryStorage(SebKortConstants.StorageKey.CARD_ID, card.getId())
                .setBalance(new Amount(currency, account.getCurrentBalance()).negate())
                .setAvailableCredit(new Amount(currency, account.getDisposableAmount()))
                .build();
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.entity;

import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
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

        return CreditCardAccount.builder(contract.getCardAccountId())
                .setAccountNumber(getMaskedCardNumber())
                .setName(contract.getProductName())
                .setBankIdentifier(contract.getCardAccountId())
                .putInTemporaryStorage(SebKortConstants.StorageKey.CARD_ID, getId())
                .setBalance(
                        new Amount(account.getCurrencyCode(), account.getCurrentBalance()).negate())
                .setAvailableCredit(
                        new Amount(account.getCurrencyCode(), account.getDisposableAmount()))
                .build();
    }
}

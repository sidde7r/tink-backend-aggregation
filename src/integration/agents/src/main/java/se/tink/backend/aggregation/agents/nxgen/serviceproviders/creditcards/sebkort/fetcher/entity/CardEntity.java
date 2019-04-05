package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.entity;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.amount.Amount;

import java.util.Map;

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

        CreditCardAccount.Builder builder = CreditCardAccount.builder(getMaskedCardNumber())
                .setAccountNumber(getMaskedCardNumber())
                .setName(contract.getProductName())
                .setHolderName(new HolderName(getNameOnCard()))
                .setBankIdentifier(contract.getId())
                .putInTemporaryStorage(SebKortConstants.StorageKey.CARD_ID, getId())
                .putInTemporaryStorage(SebKortConstants.StorageKey.CARD_CONTRACT_ID, contract.getId());

        if (account != null) {
            return (CreditCardAccount) builder
                    .setBalance(new Amount(account.getCurrencyCode(), account.getCurrentBalance()).negate())
                    .setAvailableCredit(new Amount(account.getCurrencyCode(), account.getDisposableAmount()))
                    .build();
        } else {
            return (CreditCardAccount) builder
                    .setBalance(new Amount(contract.getCurrencyCode(), contract.getNonBilledAmount()).negate())
                    .build();
        }
    }
}

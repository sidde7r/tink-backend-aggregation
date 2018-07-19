package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DebitCardsEntity {
    private String debitCardName;
    private ReferenceAccountEntity referenceAccount;
    private String cardHolder;
    private String cardNumber;
    private int cardSequenceNumber;
    private String validThru;
    private String status;
    private BalanceEntity weeklyLimit;
    private BalanceEntity dailyLimit;
    private String cardType;
    private String cardTypeShort;

    public String getDebitCardName() {
        return debitCardName;
    }

    public ReferenceAccountEntity getReferenceAccount() {
        return referenceAccount;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public int getCardSequenceNumber() {
        return cardSequenceNumber;
    }

    public String getValidThru() {
        return validThru;
    }

    public String getStatus() {
        return status;
    }

    public BalanceEntity getWeeklyLimit() {
        return weeklyLimit;
    }

    public BalanceEntity getDailyLimit() {
        return dailyLimit;
    }

    public String getCardType() {
        return cardType;
    }

    public String getCardTypeShort() {
        return cardTypeShort;
    }
}

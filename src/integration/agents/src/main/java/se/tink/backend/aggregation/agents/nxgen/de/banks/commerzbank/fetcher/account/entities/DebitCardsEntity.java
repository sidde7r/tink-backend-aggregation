package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
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
}

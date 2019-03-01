package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.creditcards.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ComboCardEntity {

    private String cardNumber;
    private String cardNumberMasked;
    private String cardType;
    private RelatedDebitAccountEntity relatedDebitAccount;
    private boolean combo;

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardNumberMasked() {
        return cardNumberMasked;
    }

    public String getCardType() {
        return cardType;
    }

    public RelatedDebitAccountEntity getRelatedDebitAccount() {
        return relatedDebitAccount;
    }

    public boolean isCombo() {
        return combo;
    }
}

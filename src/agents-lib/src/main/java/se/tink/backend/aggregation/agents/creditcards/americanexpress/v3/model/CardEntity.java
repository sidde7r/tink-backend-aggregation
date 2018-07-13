package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

public class CardEntity {
    private String cardMemberName;
    private String cardProductName;
    private String suppIndex;

    public String getCardMemberName() {
        return cardMemberName;
    }

    public void setCardMemberName(String cardMemberName) {
        this.cardMemberName = cardMemberName;
    }

    public String getCardProductName() {
        return cardProductName;
    }

    public void setCardProductName(String cardProductName) {
        this.cardProductName = cardProductName;
    }

    public String getSuppIndex() {
        return suppIndex;
    }

    public void setSuppIndex(String suppIndex) {
        this.suppIndex = suppIndex;
    }
}

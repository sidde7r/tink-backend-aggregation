package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

public class BankEntity {
    private String bankName;
    private int fromClearingRange;
    private int toClearingRange;
    private String shortRuleMessage;
    private String fullRuleMessage;

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public int getFromClearingRange() {
        return fromClearingRange;
    }

    public void setFromClearingRange(int fromClearingRange) {
        this.fromClearingRange = fromClearingRange;
    }

    public int getToClearingRange() {
        return toClearingRange;
    }

    public void setToClearingRange(int toClearingRange) {
        this.toClearingRange = toClearingRange;
    }

    public String getShortRuleMessage() {
        return shortRuleMessage;
    }

    public void setShortRuleMessage(String shortRuleMessage) {
        this.shortRuleMessage = shortRuleMessage;
    }

    public String getFullRuleMessage() {
        return fullRuleMessage;
    }

    public void setFullRuleMessage(String fullRuleMessage) {
        this.fullRuleMessage = fullRuleMessage;
    }

}

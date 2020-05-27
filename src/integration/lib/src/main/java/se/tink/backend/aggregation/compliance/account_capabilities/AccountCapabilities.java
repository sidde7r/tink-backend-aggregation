package se.tink.backend.aggregation.compliance.account_capabilities;

public class AccountCapabilities {
    public enum Answer {
        YES,
        NO,
        UNKNOWN
    }

    private Answer canWithdrawFunds = Answer.UNKNOWN;
    private Answer canPlaceFunds = Answer.UNKNOWN;
    private Answer canMakeAndReceiveTransfer = Answer.UNKNOWN;

    public void setCanWithdrawFunds(Answer canWithdrawFunds) {
        this.canWithdrawFunds = canWithdrawFunds;
    }

    public void setCanPlaceFunds(Answer canPlaceFunds) {
        this.canPlaceFunds = canPlaceFunds;
    }

    public void setCanMakeAndReceiveTransfer(Answer canMakeAndReceiveTransfer) {
        this.canMakeAndReceiveTransfer = canMakeAndReceiveTransfer;
    }

    public Answer getCanWithdrawFunds() {
        return canWithdrawFunds;
    }

    public Answer getCanPlaceFunds() {
        return canPlaceFunds;
    }

    public Answer getCanMakeAndReceiveTransfer() {
        return canMakeAndReceiveTransfer;
    }
}

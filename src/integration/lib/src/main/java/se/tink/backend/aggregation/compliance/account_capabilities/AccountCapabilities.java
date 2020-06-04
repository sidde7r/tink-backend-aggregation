package se.tink.backend.aggregation.compliance.account_capabilities;

import com.google.common.base.Preconditions;

public class AccountCapabilities {
    public enum Answer {
        YES,
        NO,
        UNKNOWN
    }

    private Answer canWithdrawFunds;
    private Answer canPlaceFunds;
    private Answer canMakeDomesticTransfer;
    private Answer canReceiveDomesticTransfer;

    private AccountCapabilities(
            Answer canWithdrawFunds,
            Answer canPlaceFunds,
            Answer canMakeDomesticTransfer,
            Answer canReceiveDomesticTransfer) {
        this.canWithdrawFunds = canWithdrawFunds;
        this.canPlaceFunds = canPlaceFunds;
        this.canMakeDomesticTransfer = canMakeDomesticTransfer;
        this.canReceiveDomesticTransfer = canReceiveDomesticTransfer;
    }

    public static AccountCapabilities createDefault() {
        return new AccountCapabilities(
                Answer.UNKNOWN, Answer.UNKNOWN, Answer.UNKNOWN, Answer.UNKNOWN);
    }

    public void setCanWithdrawFunds(Answer canWithdrawFunds) {
        Preconditions.checkNotNull(canWithdrawFunds);
        this.canWithdrawFunds = canWithdrawFunds;
    }

    public void setCanPlaceFunds(Answer canPlaceFunds) {
        Preconditions.checkNotNull(canPlaceFunds);
        this.canPlaceFunds = canPlaceFunds;
    }

    public void setCanMakeDomesticTransfer(Answer canMakeDomesticTransfer) {
        Preconditions.checkNotNull(canMakeDomesticTransfer);
        this.canMakeDomesticTransfer = canMakeDomesticTransfer;
    }

    public void setCanReceiveDomesticTransfer(Answer canReceiveDomesticTransfer) {
        Preconditions.checkNotNull(canReceiveDomesticTransfer);
        this.canReceiveDomesticTransfer = canReceiveDomesticTransfer;
    }

    public Answer getCanWithdrawFunds() {
        return canWithdrawFunds;
    }

    public Answer getCanPlaceFunds() {
        return canPlaceFunds;
    }

    public Answer getCanMakeDomesticTransfer() {
        return canMakeDomesticTransfer;
    }

    public Answer getCanReceiveDomesticTransfer() {
        return canReceiveDomesticTransfer;
    }
}

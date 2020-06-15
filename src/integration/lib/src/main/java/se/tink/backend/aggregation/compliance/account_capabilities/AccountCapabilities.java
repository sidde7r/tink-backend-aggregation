package se.tink.backend.aggregation.compliance.account_capabilities;

import com.google.common.base.Preconditions;
import java.util.Optional;

public class AccountCapabilities {
    public enum Answer {
        YES,
        NO,
        UNKNOWN;

        public static Answer From(Boolean value) {
            return Optional.ofNullable(value)
                    .map(v -> Boolean.TRUE.equals(v) ? Answer.YES : Answer.NO)
                    .orElse(Answer.UNKNOWN);
        }
    }

    /**
     * Can the account holder take out cash (e.g. over counter or in ATMs) directly from this
     * account without a need to first place the funds into some other intermediary account.
     */
    private Answer canWithdrawCash;

    /**
     * Can the account holder deposit funds directly into this account without a requirement to
     * first place the funds into some other intermediary account. For example by making a transfer
     * from another account held by the account holder at the same account holding bank (i.e.
     * internal transfer), or by making a physical deposit at a bank office or by depositing through
     * a depositing box/machine. It is therefore not important which way it is done - the important
     * part is that the account itself has a feature allowing to place funds directly into it.
     */
    private Answer canPlaceFunds;

    /**
     * Can the account holder directly* execute a transfer, such as credit transfers (both if
     * initiated in electronic channels or over the counter), via debit card(s) or direct debits, to
     * an external party.
     *
     * <p>An external party refers to some person, natural or legal, that is not the account holder
     * or not the same account holding bank. I.e. a transfer from person A's account in bank X to
     * person A's account in bank Z is considered an external transfer. The geographic location of
     * the external party does not matter, it can be domestic or international.
     *
     * <p>*: If the use of an intermediate account is necessary, meaning that the account holder
     * first has to move money to another account and then make the transfer from the second
     * account, then the first account cannot execute an external transfer.
     */
    private Answer canExecuteExternalTransfer;

    /**
     * Can the account receive transfers, such as credit transfers or direct debits, from an
     * external party.
     *
     * <p>An external party refers to some person, natural or legal, that is not the account holder
     * or not the same account holding bank. I.e. a transfer from person A's account in bank X to
     * person A's account in bank Z is considered an external transfer. The geographic location of
     * the external party does not matter, it can be domestic or international.
     */
    private Answer canReceiveExternalTransfer;

    private AccountCapabilities(
            Answer canWithdrawCash,
            Answer canPlaceFunds,
            Answer canExecuteExternalTransfer,
            Answer canReceiveExternalTransfer) {
        this.canWithdrawCash = canWithdrawCash;
        this.canPlaceFunds = canPlaceFunds;
        this.canExecuteExternalTransfer = canExecuteExternalTransfer;
        this.canReceiveExternalTransfer = canReceiveExternalTransfer;
    }

    public static AccountCapabilities createDefault() {
        return new AccountCapabilities(
                Answer.UNKNOWN, Answer.UNKNOWN, Answer.UNKNOWN, Answer.UNKNOWN);
    }

    public void setCanWithdrawCash(Answer canWithdrawCash) {
        Preconditions.checkNotNull(canWithdrawCash);
        this.canWithdrawCash = canWithdrawCash;
    }

    public void setCanPlaceFunds(Answer canPlaceFunds) {
        Preconditions.checkNotNull(canPlaceFunds);
        this.canPlaceFunds = canPlaceFunds;
    }

    public void setCanExecuteExternalTransfer(Answer canExecuteExternalTransfer) {
        Preconditions.checkNotNull(canExecuteExternalTransfer);
        this.canExecuteExternalTransfer = canExecuteExternalTransfer;
    }

    public void setCanReceiveExternalTransfer(Answer canReceiveExternalTransfer) {
        Preconditions.checkNotNull(canReceiveExternalTransfer);
        this.canReceiveExternalTransfer = canReceiveExternalTransfer;
    }

    public Answer getCanWithdrawCash() {
        return canWithdrawCash;
    }

    public Answer getCanPlaceFunds() {
        return canPlaceFunds;
    }

    public Answer getCanExecuteExternalTransfer() {
        return canExecuteExternalTransfer;
    }

    public Answer getCanReceiveExternalTransfer() {
        return canReceiveExternalTransfer;
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebConstants.AccountTypeCode;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;

public class SebPaymentAccountCapabilities {

    private SebPaymentAccountCapabilities() {
        throw new AssertionError();
    }

    public static AccountCapabilities.Answer canWithdrawCash(String accountTypeCode) {
        if (accountTypeCode.equalsIgnoreCase(AccountTypeCode.OTHER)) {}

        return SebConstants.AccountCapabilities.CAN_WITHDRAW_CASH_MAPPER
                .translate(accountTypeCode)
                .orElse(AccountCapabilities.Answer.UNKNOWN);
    }

    public static AccountCapabilities.Answer canPlaceFunds(String accountTypeCode) {
        return SebConstants.AccountCapabilities.CAN_PLACE_FUNDS_MAPPER
                .translate(accountTypeCode)
                .orElse(AccountCapabilities.Answer.UNKNOWN);
    }

    public static AccountCapabilities.Answer canExecuteExternalTransfer(String accountTypeCode) {
        return SebConstants.AccountCapabilities.CAN_EXECUTE_EXTERNAL_TRANSFER_MAPPER
                .translate(accountTypeCode)
                .orElse(AccountCapabilities.Answer.UNKNOWN);
    }

    public static AccountCapabilities.Answer canReceiveExternalTransfer(String accountTypeCode) {
        return SebConstants.AccountCapabilities.CAN_RECEIVE_EXTERNAL_TRANSFER_MAPPER
                .translate(accountTypeCode)
                .orElse(AccountCapabilities.Answer.UNKNOWN);
    }
}

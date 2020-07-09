package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb;

import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities.Answer;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public class SebPaymentAccountCapabilities {

    private SebPaymentAccountCapabilities() {
        throw new AssertionError();
    }

    public static Answer canWithdrawCash(String accountTypeCode, String accountTypeName) {
        return translate(
                SebConstants.AccountCapabilities.CAN_WITHDRAW_CASH_MAPPER,
                accountTypeCode,
                accountTypeName);
    }

    public static Answer canPlaceFunds(String accountTypeCode, String accountTypeName) {
        return translate(
                SebConstants.AccountCapabilities.CAN_PLACE_FUNDS_MAPPER,
                accountTypeCode,
                accountTypeName);
    }

    public static Answer canExecuteExternalTransfer(
            String accountTypeCode, String accountTypeName) {
        return translate(
                SebConstants.AccountCapabilities.CAN_EXECUTE_EXTERNAL_TRANSFER_MAPPER,
                accountTypeCode,
                accountTypeName);
    }

    public static Answer canReceiveExternalTransfer(
            String accountTypeCode, String accountTypeName) {
        return translate(
                SebConstants.AccountCapabilities.CAN_RECEIVE_EXTERNAL_TRANSFER_MAPPER,
                accountTypeCode,
                accountTypeName);
    }

    private static Answer translate(
            TypeMapper<Answer> mapper, String accountTypeCode, String accountTypeName) {
        // first try specific mapping of "code:name", fallback to code only if not found
        final String accountTypeCodeAndName =
                String.format("%s:%s", accountTypeCode, accountTypeName);
        return mapper.translate(accountTypeCodeAndName)
                .orElseGet(() -> mapper.translate(accountTypeCode).orElse(Answer.UNKNOWN));
    }
}

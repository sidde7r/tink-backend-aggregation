package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.PaymentTransfer;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.entities.PaymentSourceAccount;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.transfer.rpc.Transfer;

public class SkandiaBankenExecutorUtils {

    public static PaymentSourceAccount tryFindOwnAccount(
            AccountIdentifier accountIdentifier, Collection<PaymentSourceAccount> accounts) {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList()).stream()
                .filter(
                        account ->
                                accountIdentifier
                                        .getIdentifier()
                                        .equals(account.getBankAccountNumber()))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    public static String formatGiroNumber(Transfer transfer) {
        int offset = calculatePaymentOffset(transfer);

        return new StringBuilder(transfer.getDestination().getIdentifier())
                .insert(offset, "-")
                .toString();
    }

    public static int calculatePaymentOffset(Transfer transfer) {
        if (isBankGiroPayment(transfer)) {
            return calculateBankGiroOffset(transfer);
        } else if (isPlusGiroPayment(transfer)) {
            return calculatePlusGiroOffset();
        }
        throw new NotImplementedException("Offset for non BG/PG payments is not implemented");
    }

    private static int calculateBankGiroOffset(Transfer transfer) {
        if (isShortBankGiroNumber(transfer)) {
            return PaymentTransfer.SE_BG_SHORT_OFFSET;
        } else if (isLongBankGiroNumber(transfer)) {
            return PaymentTransfer.SE_BG_LONG_OFFSET;
        } else {
            throw new IllegalArgumentException(
                    String.format(
                            "Gironumber needs to be of length %s or %s",
                            PaymentTransfer.SE_BG_MINIMUM_LENGTH,
                            PaymentTransfer.SE_BG_MAXIMUM_LENGTH));
        }
    }

    private static int calculatePlusGiroOffset() {
        return PaymentTransfer.SE_PG_OFFSET;
    }

    private static boolean isBankGiroPayment(Transfer transfer) {
        return transfer.getDestination().getType().equals(AccountIdentifierType.SE_BG);
    }

    private static boolean isPlusGiroPayment(Transfer transfer) {
        return transfer.getDestination().getType().equals(AccountIdentifierType.SE_PG);
    }

    private static boolean isShortBankGiroNumber(Transfer transfer) {
        return transfer.getDestination().getIdentifier().length()
                == PaymentTransfer.SE_BG_MINIMUM_LENGTH;
    }

    private static boolean isLongBankGiroNumber(Transfer transfer) {
        return transfer.getDestination().getIdentifier().length()
                == PaymentTransfer.SE_BG_MAXIMUM_LENGTH;
    }
}

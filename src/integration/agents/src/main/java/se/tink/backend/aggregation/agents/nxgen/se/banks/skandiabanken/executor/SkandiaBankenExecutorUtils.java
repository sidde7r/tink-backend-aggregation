package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.entities.PaymentSourceAccount;
import se.tink.libraries.account.AccountIdentifier;

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
}

package se.tink.backend.utils;

import java.util.Optional;
import se.tink.libraries.account.identifiers.se.ClearingNumber;

public interface ClearingNumberBankToProviderMap {
    Optional<String> getProviderForBank(ClearingNumber.Bank bank);
}

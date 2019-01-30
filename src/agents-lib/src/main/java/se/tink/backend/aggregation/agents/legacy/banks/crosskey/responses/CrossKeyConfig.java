package se.tink.backend.aggregation.agents.banks.crosskey.responses;

import se.tink.backend.aggregation.agents.banks.crosskey.PaginationTypes;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.agents.rpc.AccountTypes;

import java.util.List;

public interface CrossKeyConfig {
    AccountTypes getAccountType(String accountGroup, String usageType);
    List<AccountIdentifier> getIdentifiers(String bic, String iban, String bban);
    PaginationTypes getPaginationType();
}

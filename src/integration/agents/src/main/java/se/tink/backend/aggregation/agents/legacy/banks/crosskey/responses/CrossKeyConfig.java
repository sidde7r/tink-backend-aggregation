package se.tink.backend.aggregation.agents.banks.crosskey.responses;

import java.util.List;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.banks.crosskey.PaginationTypes;
import se.tink.libraries.account.AccountIdentifier;

public interface CrossKeyConfig {
    AccountTypes getAccountType(String accountGroup, String usageType);

    List<AccountIdentifier> getIdentifiers(String bic, String iban, String bban);

    PaginationTypes getPaginationType();
}

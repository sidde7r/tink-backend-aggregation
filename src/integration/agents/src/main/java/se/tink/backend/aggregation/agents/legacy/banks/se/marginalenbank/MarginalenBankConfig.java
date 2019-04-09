package se.tink.backend.aggregation.agents.banks.se.marginalenbank;

import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.banks.crosskey.PaginationTypes;
import se.tink.backend.aggregation.agents.banks.crosskey.responses.CrossKeyConfig;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

public class MarginalenBankConfig implements CrossKeyConfig {
    @Override
    public AccountTypes getAccountType(String accountGroup, String usageType) {

        // This method is intended empty, because of we do not use this bank.
        // Please, make an implementation, when we start to use it
        return AccountTypes.OTHER;
    }

    @Override
    public List<AccountIdentifier> getIdentifiers(String bic, String iban, String bban) {
        List<AccountIdentifier> identifiers = Lists.newArrayList();

        identifiers.add(new SwedishIdentifier(bban));

        return identifiers;
    }

    @Override
    public PaginationTypes getPaginationType() {
        return PaginationTypes.NONE;
    }
}

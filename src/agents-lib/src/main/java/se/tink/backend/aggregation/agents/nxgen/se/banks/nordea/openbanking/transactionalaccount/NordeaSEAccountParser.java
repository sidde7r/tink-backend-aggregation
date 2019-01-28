package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.openbanking.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.NordeaAccountParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.entities.AccountEntity;

public class NordeaSEAccountParser extends NordeaAccountParser {

    @Override
    protected String getAccountNumber(AccountEntity accountEntity) {
        return findAccountNumberByType(accountEntity, NordeaBaseConstants.Account.ACCOUNT_NUMBER_SE)
                .orElseThrow(() -> new IllegalStateException("No account number found"));
    }
}

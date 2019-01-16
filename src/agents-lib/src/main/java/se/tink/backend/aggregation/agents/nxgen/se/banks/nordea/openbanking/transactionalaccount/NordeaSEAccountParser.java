package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.openbanking.transactionalaccount;

import java.util.Collections;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.NordeaAccountParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.entities.AccountNumbersEntity;

public class NordeaSEAccountParser extends NordeaAccountParser {

    @Override
    protected String getAccountNumber(AccountEntity accountEntity) {
        return Optional.ofNullable(accountEntity.getAccountNumbers()).orElse(Collections.emptyList()).stream()
                .filter(accountNumberEntity ->
                        NordeaBaseConstants.Account.ACCOUNT_NUMBER_SE.equalsIgnoreCase(accountNumberEntity.getType())
                                && hasContent(accountNumberEntity.getValue()))
                .map(AccountNumbersEntity::getValue)
                .findFirst().orElseThrow(() -> new IllegalStateException("No account number found"));
    }
}

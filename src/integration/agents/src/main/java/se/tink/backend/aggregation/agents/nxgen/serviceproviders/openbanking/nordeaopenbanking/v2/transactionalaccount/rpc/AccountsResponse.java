package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.rpc;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.rpc.NordeaResponseBase;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.NordeaAccountParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.entities.AccountsResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

@JsonObject
public class AccountsResponse extends NordeaResponseBase {
    private AccountsResponseEntity response;

    public List<TransactionalAccount> getTinkAccounts(NordeaAccountParser accountParser) {
        if (response == null || response.getAccounts() == null) {
            return Collections.emptyList();
        }
        return response.getAccounts().stream()
                .filter(AccountEntity::isOpen)
                .filter(account -> NordeaBaseConstants.ACCOUNT_TYPE.isTransactionalAccount(account.tinkAccountType()))
                .map(accountParser::toTinkAccount)
                .collect(Collectors.toList());
    }
}

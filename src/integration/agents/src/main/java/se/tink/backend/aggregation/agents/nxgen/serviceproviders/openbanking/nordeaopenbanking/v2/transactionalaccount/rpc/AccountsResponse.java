package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.rpc.NordeaResponseBase;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.NordeaAccountParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.entities.AccountsResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountsResponse extends NordeaResponseBase {
    @JsonIgnore
    private static BiPredicate<AccountEntity, List<AccountTypes>> isOneOfType =
            (account, types) -> types.contains(account.tinkAccountType());

    private AccountsResponseEntity response;

    public List<TransactionalAccount> getTinkAccounts(NordeaAccountParser accountParser) {
        if (response == null || response.getAccounts() == null) {
            return Collections.emptyList();
        }
        return response.getAccounts().stream()
                .filter(AccountEntity::isOpen)
                .filter(
                        account ->
                                isOneOfType.test(
                                        account,
                                        Arrays.asList(AccountTypes.CHECKING, AccountTypes.SAVINGS)))
                .map(accountParser::toTinkAccount)
                .collect(Collectors.toList());
    }
}

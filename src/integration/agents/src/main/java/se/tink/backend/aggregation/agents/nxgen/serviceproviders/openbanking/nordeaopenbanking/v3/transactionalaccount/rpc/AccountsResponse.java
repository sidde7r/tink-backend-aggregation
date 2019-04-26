package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.entities.GroupHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.entities.LinkListEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.NordeaAccountParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.entities.AccountsResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountsResponse {
    @JsonIgnore
    private static BiPredicate<AccountEntity, List<AccountTypes>> isOneOfType =
            (account, types) -> types.contains(account.tinkAccountType());

    @JsonProperty("group_header")
    private GroupHeaderEntity groupHeader;

    private AccountsResponseEntity response;

    @JsonProperty("_links")
    private LinkListEntity links;

    public GroupHeaderEntity getGroupHeader() {
        return groupHeader;
    }

    public List<TransactionalAccount> getTinkAccounts(NordeaAccountParser accountParser) {
        return Optional.ofNullable(response).map(r -> r.getAccounts())
                .orElseGet(Collections::emptyList).stream()
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

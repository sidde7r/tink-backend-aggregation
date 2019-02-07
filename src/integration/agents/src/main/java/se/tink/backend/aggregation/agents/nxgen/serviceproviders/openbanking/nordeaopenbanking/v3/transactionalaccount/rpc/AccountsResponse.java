package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.entities.GroupHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.entities.LinkListEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.NordeaAccountParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.entities.AccountsResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountsResponse {
    @JsonProperty("group_header")
    private GroupHeaderEntity groupHeader;
    private AccountsResponseEntity response;
    @JsonProperty("_links")
    private LinkListEntity links;

    public GroupHeaderEntity getGroupHeader() {
        return groupHeader;
    }

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

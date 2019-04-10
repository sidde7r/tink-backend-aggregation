package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class FetchTransactionalAccountResponse {
    @JsonProperty("result")
    private List<AccountEntity> accounts;

    public List<TransactionalAccount> toTinkAccount() {

        return accounts.stream()
                .filter(
                        entity ->
                                NordeaFIConstants.ACCOUNT_TYPE_MAPPER
                                        .translate(entity.getCategory())
                                        .equals(Optional.of(AccountTypes.CHECKING)))
                .filter(
                        entity ->
                                entity.permissions
                                        .isCanPayFromAccount()) // filter ISK accounts marked as
                // "savings"
                .map(AccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }
}

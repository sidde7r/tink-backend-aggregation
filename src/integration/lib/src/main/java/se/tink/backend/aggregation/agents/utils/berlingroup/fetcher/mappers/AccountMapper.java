package se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.mappers;

import java.util.Optional;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public interface AccountMapper {
    Optional<TransactionalAccount> toTinkAccount(AccountEntity accountEntity);
}

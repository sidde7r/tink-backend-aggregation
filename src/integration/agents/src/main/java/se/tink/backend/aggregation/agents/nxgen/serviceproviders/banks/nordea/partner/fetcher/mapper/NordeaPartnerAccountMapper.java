package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.mapper;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity.AccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public interface NordeaPartnerAccountMapper {

    Optional<TransactionalAccount> toTinkTransactionalAccount(AccountEntity accountEntity);
}

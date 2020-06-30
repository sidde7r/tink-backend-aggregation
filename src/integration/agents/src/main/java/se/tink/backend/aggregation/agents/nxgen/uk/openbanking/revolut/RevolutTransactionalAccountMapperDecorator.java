package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.revolut;

import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.AccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.transactionalaccounts.TransactionalAccountMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
@Slf4j
public class RevolutTransactionalAccountMapperDecorator
        implements AccountMapper<TransactionalAccount> {

    private static final String NO_IDENTIFIER_FOUND_LOG_MESSAGE_FORMAT =
            "No valid id for account with accountNickname: {}, accountDescription: {}. Skipping account mapping.";

    private final TransactionalAccountMapper transactionalAccountMapper;

    @Override
    public boolean supportsAccountType(AccountTypes type) {
        return transactionalAccountMapper.supportsAccountType(type);
    }

    @Override
    public Optional<TransactionalAccount> map(
            AccountEntity account,
            Collection<AccountBalanceEntity> balances,
            Collection<IdentityDataV31Entity> parties) {

        if (account.getIdentifiers().isEmpty()) {
            log.debug(
                    NO_IDENTIFIER_FOUND_LOG_MESSAGE_FORMAT,
                    account.getNickname(),
                    account.getDescription());
            return Optional.empty();
        }

        return transactionalAccountMapper.map(account, balances, parties);
    }
}

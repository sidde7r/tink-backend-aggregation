package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank.DanskeConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.transactionalaccounts.TransactionalAccountBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.transactionalaccounts.TransactionalAccountMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class DkTransactionalAccountMapper extends TransactionalAccountMapper {

    private final DanskeDkIdentifierMapper danskeDkIdentifierMapper;

    public DkTransactionalAccountMapper(
            TransactionalAccountBalanceMapper balanceMapper,
            DanskeDkIdentifierMapper danskeDkIdentifierMapper) {
        super(balanceMapper, danskeDkIdentifierMapper);
        this.danskeDkIdentifierMapper = danskeDkIdentifierMapper;
    }

    @Override
    public Optional<TransactionalAccount> map(
            AccountEntity account,
            Collection<AccountBalanceEntity> balances,
            Collection<IdentityDataV31Entity> parties) {
        List<AccountIdentifierEntity> accountIdentifiers = account.getIdentifiers();

        AccountIdentifierEntity primaryIdentifier =
                danskeDkIdentifierMapper.getTransactionalAccountPrimaryIdentifier(
                        accountIdentifiers,
                        DanskeConstants.ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS);
        String accountNumber =
                danskeDkIdentifierMapper.formatIdentificationNumber(primaryIdentifier);

        return buildTransactionalAccount(
                account,
                balances,
                parties,
                accountIdentifiers,
                primaryIdentifier,
                primaryIdentifier.getIdentification(),
                accountNumber);
    }
}

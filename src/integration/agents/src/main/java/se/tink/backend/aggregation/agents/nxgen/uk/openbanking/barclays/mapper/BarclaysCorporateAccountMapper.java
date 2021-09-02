package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.mapper;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountTypeMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.IdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.transactionalaccounts.TransactionalAccountBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.transactionalaccounts.TransactionalAccountMapper;
import se.tink.backend.aggregation.nxgen.core.account.AccountHolderType;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public class BarclaysCorporateAccountMapper extends TransactionalAccountMapper {

    private final AccountTypeMapper accountTypeMapper;

    public BarclaysCorporateAccountMapper(
            TransactionalAccountBalanceMapper balanceMapper,
            IdentifierMapper identifierMapper,
            AccountTypeMapper accountTypeMapper) {
        super(balanceMapper, identifierMapper);
        this.accountTypeMapper = accountTypeMapper;
    }

    @Override
    public AccountHolderType mapAccountHolderType(AccountEntity account) {
        return AccountHolderType.BUSINESS;
    }

    @Override
    public TransactionalAccountType mapTransactionalAccountType(AccountEntity account) {
        AccountTypes accountType = accountTypeMapper.getAccountType(account);
        if (accountType == AccountTypes.CHECKING) {
            return TransactionalAccountType.CHECKING;
        } else if (accountType == AccountTypes.SAVINGS) {
            return TransactionalAccountType.SAVINGS;
        }
        throw new IllegalStateException(
                "Cannot map to transactional account. Wrong account type passed to the mapper");
    }
}

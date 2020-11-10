package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.mapper;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.DanskebankV31Constant;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.transactionalaccounts.TransactionalAccountBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.transactionalaccounts.TransactionalAccountMapper;

public class DanskeTransactionalAccountMapper extends TransactionalAccountMapper {

    private final DanskeIdentifierMapper danskeIdentifierMapper;

    public DanskeTransactionalAccountMapper(
            TransactionalAccountBalanceMapper balanceMapper,
            DanskeIdentifierMapper danskeIdentifierMapper) {
        super(balanceMapper, danskeIdentifierMapper);
        this.danskeIdentifierMapper = danskeIdentifierMapper;
    }

    @Override
    protected String getUniqueIdentifier(AccountIdentifierEntity primaryIdentifier) {
        return danskeIdentifierMapper.formatIdentificationNumber(primaryIdentifier);
    }

    @Override
    protected List<UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code>
            getAllowedTransactionalAccountIdentifiers() {
        return DanskebankV31Constant.ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS;
    }
}

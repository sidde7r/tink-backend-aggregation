package se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.mapper;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper.AccountEntityMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.enums.MarketCode;

public class NoAccountEntityMapper extends AccountEntityMapper {

    public NoAccountEntityMapper() {
        super(MarketCode.NO.name());
    }

    @Override
    protected String getUniqueIdentifier(AccountEntity accountEntity) {
        return accountEntity.getAccountNoExt();
    }

    @Override
    protected AccountIdentifierType getAccountIdentifierType(String marketCode) {
        return AccountIdentifierType.BBAN;
    }
}

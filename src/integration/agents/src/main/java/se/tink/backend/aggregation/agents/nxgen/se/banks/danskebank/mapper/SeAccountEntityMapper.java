package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.mapper;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper.AccountEntityMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.enums.MarketCode;

public class SeAccountEntityMapper extends AccountEntityMapper {

    public SeAccountEntityMapper() {
        super(MarketCode.SE.name());
    }

    @Override
    protected String getUniqueIdentifier(AccountEntity accountEntity) {
        return accountEntity.getAccountNoExt();
    }

    @Override
    protected AccountIdentifier.Type getAccountIdentifierType(String marketCode) {
        return AccountIdentifier.Type.BBAN;
    }
}

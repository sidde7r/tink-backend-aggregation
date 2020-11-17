package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.mapper;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.CreditCardAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.CreditCardBalanceMapper;

public class DanskeCreditCardAccountMapper extends CreditCardAccountMapper {

    private final DanskeIdentifierMapper danskeIdentifierMapper;

    public DanskeCreditCardAccountMapper(
            CreditCardBalanceMapper balanceMapper, DanskeIdentifierMapper danskeIdentifierMapper) {
        super(balanceMapper, danskeIdentifierMapper);
        this.danskeIdentifierMapper = danskeIdentifierMapper;
    }

    @Override
    protected String getUniqueIdentifier(AccountIdentifierEntity cardIdentifier) {
        return danskeIdentifierMapper.formatIdentificationNumber(cardIdentifier);
    }
}

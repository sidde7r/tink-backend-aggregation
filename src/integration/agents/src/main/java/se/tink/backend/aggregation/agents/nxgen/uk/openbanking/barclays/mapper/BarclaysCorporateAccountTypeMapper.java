package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.mapper;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountTypeMapper;

public class BarclaysCorporateAccountTypeMapper implements AccountTypeMapper {

    @Override
    public AccountTypes getAccountType(AccountEntity accountEntity) {
        return ACCOUNT_TYPE_MAPPER
                .translate(accountEntity.getRawAccountSubType())
                .orElse(AccountTypes.CHECKING); // temporary fallback for missing AccountSubType
    }

    @Override
    public AccountOwnershipType getAccountOwnershipType(AccountEntity account) {
        return AccountOwnershipType.BUSINESS;
    }

    @Override
    public boolean supportsAccountOwnershipType(AccountEntity accountEntity) {
        return true;
    }
}

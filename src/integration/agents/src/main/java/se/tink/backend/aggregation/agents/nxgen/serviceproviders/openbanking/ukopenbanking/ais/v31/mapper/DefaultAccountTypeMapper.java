package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;

public class DefaultAccountTypeMapper implements AccountTypeMapper {

    private final UkOpenBankingAisConfig aisConfig;

    public DefaultAccountTypeMapper(UkOpenBankingAisConfig aisConfig) {
        this.aisConfig = Preconditions.checkNotNull(aisConfig);
    }

    public boolean supportsAccountOwnershipType(AccountEntity accountEntity) {
        return aisConfig
                .getAllowedAccountOwnershipTypes()
                .contains(getAccountOwnershipType(accountEntity));
    }
}

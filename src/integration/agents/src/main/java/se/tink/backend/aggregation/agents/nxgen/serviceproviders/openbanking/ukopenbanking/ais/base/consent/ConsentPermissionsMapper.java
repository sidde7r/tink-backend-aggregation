package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent.mappers.AccountsPermissionsMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent.mappers.BeneficiariesPermissionsMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent.mappers.IdentityDataPermissionsMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent.mappers.PermissionsMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent.mappers.TransactionsPermissionsMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.libraries.credentials.service.RefreshableItem;

public class ConsentPermissionsMapper {

    private final Set<PermissionsMapper> mappers;
    private final UkOpenBankingAisConfig aisConfig;

    public ConsentPermissionsMapper(UkOpenBankingAisConfig aisConfig) {
        this.mappers =
                Sets.newHashSet(
                        new AccountsPermissionsMapper(aisConfig),
                        new TransactionsPermissionsMapper(),
                        new IdentityDataPermissionsMapper(aisConfig),
                        new BeneficiariesPermissionsMapper());
        this.aisConfig = aisConfig;
    }

    public ImmutableSet<String> mapFrom(Set<RefreshableItem> itemsToRefresh) {
        return mappers.stream()
                .map(mapper -> mapper.mapFrom(itemsToRefresh))
                .flatMap(Collection::stream)
                .map(ConsentPermission::getValue)
                // TODO: Filtering to be removed when Core trims RefreshScope, so it respects agent
                // capabilities
                .filter(permission -> aisConfig.getPermissions().contains(permission))
                .collect(ImmutableSet.toImmutableSet());
    }
}

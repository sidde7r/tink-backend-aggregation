package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent.mappers.AccountsPermissionsMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent.mappers.BeneficiariesPermissionsMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent.mappers.IdentityDataPermissionsMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent.mappers.PermissionsMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent.mappers.TransactionsPermissionsMapper;
import se.tink.libraries.credentials.service.RefreshableItem;

public class ConsentPermissionsMapper {

    private final Set<PermissionsMapper> mappers;

    public ConsentPermissionsMapper(UkOpenBankingAisConfig aisConfig) {
        this.mappers =
                Sets.newHashSet(
                        new AccountsPermissionsMapper(),
                        new TransactionsPermissionsMapper(),
                        new IdentityDataPermissionsMapper(aisConfig),
                        new BeneficiariesPermissionsMapper());
    }

    public ConsentPermissionsMapper(Set<PermissionsMapper> mappers) {
        this.mappers = mappers;
    }

    public ImmutableSet<String> mapFrom(Set<RefreshableItem> itemsToRefresh) {
        return mappers.stream()
                .map(mapper -> mapper.mapFrom(itemsToRefresh))
                .flatMap(Collection::stream)
                .map(ConsentPermission::getValue)
                .collect(ImmutableSet.toImmutableSet());
    }
}

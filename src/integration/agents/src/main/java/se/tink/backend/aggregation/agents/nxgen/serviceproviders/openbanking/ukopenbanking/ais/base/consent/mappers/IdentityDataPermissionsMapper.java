package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent.mappers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent.ConsentPermission;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.libraries.credentials.service.RefreshableItem;

public class IdentityDataPermissionsMapper implements PermissionsMapper {

    private final UkOpenBankingAisConfig aisConfig;

    public IdentityDataPermissionsMapper(UkOpenBankingAisConfig aisConfig) {
        this.aisConfig = aisConfig;
    }

    @Override
    public Set<ConsentPermission> mapFrom(Set<RefreshableItem> items) {
        if (!items.contains(RefreshableItem.IDENTITY_DATA)) {
            return Collections.emptySet();
        }

        return getPermissions();
    }

    private Set<ConsentPermission> getPermissions() {
        Set<ConsentPermission> permissions = new HashSet<>();

        if (aisConfig.isPartyEndpointEnabled()) {
            permissions.add(ConsentPermission.READ_PARTY_PSU);
        }

        return permissions;
    }
}

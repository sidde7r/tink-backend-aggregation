package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent.mappers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent.ConsentPermission;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.libraries.credentials.service.RefreshableItem;

public class AccountsPermissionsMapper implements PermissionsMapper {

    private final UkOpenBankingAisConfig aisConfig;

    public AccountsPermissionsMapper(UkOpenBankingAisConfig aisConfig) {
        this.aisConfig = aisConfig;
    }

    @Override
    public Set<ConsentPermission> mapFrom(Set<RefreshableItem> items) {
        if (!RefreshableItem.hasAccounts(items)) {
            return Collections.emptySet();
        }

        return getPermissions();
    }

    private Set<ConsentPermission> getPermissions() {
        Set<ConsentPermission> permissions = new HashSet<>();
        permissions.add(ConsentPermission.READ_ACCOUNTS_DETAIL);
        permissions.add(ConsentPermission.READ_BALANCES);

        if (aisConfig.isAccountPartiesEndpointEnabled()
                || aisConfig.isAccountPartyEndpointEnabled()) {
            permissions.add(ConsentPermission.READ_PARTY);
        }

        return permissions;
    }
}

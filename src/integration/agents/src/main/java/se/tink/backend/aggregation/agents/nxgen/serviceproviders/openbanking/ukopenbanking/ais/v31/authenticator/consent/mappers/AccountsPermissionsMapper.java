package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent.mappers;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent.ConsentPermission;
import se.tink.libraries.credentials.service.RefreshableItem;

public class AccountsPermissionsMapper implements PermissionsMapper {

    private static final Set<ConsentPermission> permissions =
            Sets.newHashSet(
                    ConsentPermission.READ_ACCOUNTS_DETAIL, ConsentPermission.READ_BALANCES);

    @Override
    public Set<ConsentPermission> mapFrom(Set<RefreshableItem> items) {
        if (!RefreshableItem.hasAccounts(items)) {
            return Collections.emptySet();
        }
        return permissions;
    }
}

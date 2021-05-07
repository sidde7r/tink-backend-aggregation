package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent.mappers;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent.ConsentPermission;
import se.tink.libraries.credentials.service.RefreshableItem;

public class BeneficiariesPermissionsMapper implements PermissionsMapper {

    private static final Set<ConsentPermission> permissions =
            Sets.newHashSet(ConsentPermission.READ_BENEFICIARIES_DETAIL);

    @Override
    public Set<ConsentPermission> mapFrom(Set<RefreshableItem> items) {
        if (missingBeneficiariesAndTransferDestinations(items)) {
            return Collections.emptySet();
        }

        return permissions;
    }

    private boolean missingBeneficiariesAndTransferDestinations(Set<RefreshableItem> items) {
        return !items.contains(RefreshableItem.LIST_BENEFICIARIES)
                && !items.contains(RefreshableItem.TRANSFER_DESTINATIONS);
    }
}

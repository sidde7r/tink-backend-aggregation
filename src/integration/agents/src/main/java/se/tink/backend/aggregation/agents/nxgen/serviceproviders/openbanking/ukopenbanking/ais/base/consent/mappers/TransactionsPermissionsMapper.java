package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent.mappers;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent.ConsentPermission;
import se.tink.libraries.credentials.service.RefreshableItem;

public class TransactionsPermissionsMapper implements PermissionsMapper {

    private static final Set<ConsentPermission> permissions =
            Sets.newHashSet(
                    ConsentPermission.READ_TRANSACTIONS_DETAIL,
                    ConsentPermission.READ_TRANSACTIONS_DEBITS,
                    ConsentPermission.READ_TRANSACTIONS_CREDITS);

    public Set<ConsentPermission> mapFrom(Set<RefreshableItem> items) {
        if (!RefreshableItem.hasTransactions(items)) {
            return Collections.emptySet();
        }

        return permissions;
    }
}

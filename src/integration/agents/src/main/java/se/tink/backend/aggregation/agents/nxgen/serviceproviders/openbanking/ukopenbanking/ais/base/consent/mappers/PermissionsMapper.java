package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent.mappers;

import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent.ConsentPermission;
import se.tink.libraries.credentials.service.RefreshableItem;

public interface PermissionsMapper {

    Set<ConsentPermission> mapFrom(Set<RefreshableItem> items);
}

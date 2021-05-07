package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent.mappers;

import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.consent.ConsentPermission;
import se.tink.libraries.credentials.service.RefreshableItem;

public interface PermissionsMapper {

    Set<ConsentPermission> mapFrom(Set<RefreshableItem> items);
}

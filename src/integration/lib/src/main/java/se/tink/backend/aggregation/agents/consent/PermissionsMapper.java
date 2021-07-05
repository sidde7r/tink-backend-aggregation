package se.tink.backend.aggregation.agents.consent;

import java.util.Set;
import se.tink.libraries.credentials.service.RefreshableItem;

public interface PermissionsMapper {

    Set<Permission> getPermissions(RefreshableItem item);
}

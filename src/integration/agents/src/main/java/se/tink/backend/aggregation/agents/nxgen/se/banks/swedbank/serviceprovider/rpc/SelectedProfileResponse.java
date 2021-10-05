package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import java.util.Map;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class SelectedProfileResponse {
    private SelectedProfileEntity selectedProfile;
    private Map<String, MenuItemLinkEntity> menuItems;
    private boolean mobile;
    private boolean nibprimaryUser;
    private String cacheGroup;
}

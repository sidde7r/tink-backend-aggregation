package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SelectedProfileResponse {
    private SelectedProfileEntity selectedProfile;
    private Map<String, MenuItemLinkEntity> menuItems;
    private boolean mobile;
    private boolean nibprimaryUser;
    private String cacheGroup;

    public SelectedProfileEntity getSelectedProfile() {
        return selectedProfile;
    }

    public Map<String, MenuItemLinkEntity> getMenuItems() {
        return menuItems;
    }

    public boolean isMobile() {
        return mobile;
    }

    public boolean isNibprimaryUser() {
        return nibprimaryUser;
    }

    public String getCacheGroup() {
        return cacheGroup;
    }

    @JsonIgnore
    public Optional<MenuItemLinkEntity> getMenuItem(String menuItemIdentifier) {
        if (menuItems == null) {
            throw new IllegalStateException();
        }

        return Optional.ofNullable(menuItems.get(menuItemIdentifier))
                .filter(MenuItemLinkEntity::isAuthorized);
    }
}

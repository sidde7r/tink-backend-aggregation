package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileMenu {
    private Map<String, MenuItem> menuItems;

    public Map<String, MenuItem> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(Map<String, MenuItem> menuItems) {
        this.menuItems = menuItems;
    }

    public boolean hasMenuItem(String menuKey) {
        return menuItems.containsKey(menuKey);
    }

    public MenuItem getMenuItem(String menuKey) {
        return menuItems.get(menuKey);
    }
}

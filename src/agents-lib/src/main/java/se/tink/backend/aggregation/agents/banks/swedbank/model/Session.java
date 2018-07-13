package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.utils.CookieContainer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Session extends CookieContainer {

    private ProfileMenu profileMenu;

    public void setProfileMenu(ProfileMenu profileMenu) {
        this.profileMenu = profileMenu;
    }

    public ProfileMenu getProfileMenu() {
        return profileMenu;
    }

}

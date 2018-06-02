package se.tink.backend.system.rpc;

import java.util.List;

import se.tink.backend.core.UserFacebookProfile;

public class UpdateFacebookProfilesRequest {
    private List<UserFacebookProfile> facebookProfiles;

    public List<UserFacebookProfile> getFacebookProfiles() {
        return facebookProfiles;
    }

    public void setFacebookProfiles(List<UserFacebookProfile> facebookProfiles) {
        this.facebookProfiles = facebookProfiles;
    }
}

package se.tink.backend.rpc;

import java.util.List;

import se.tink.backend.core.UserLocation;

public class ReportLocationRequest {
    private List<UserLocation> userLocations;

    public List<UserLocation> getUserLocations() {
        return userLocations;
    }

    public void setUserLocations(List<UserLocation> userLocations) {
        this.userLocations = userLocations;
    }
}

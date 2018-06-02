package se.tink.backend.aggregationcontroller.v1.rpc.entities;

public class UserProfile {
    private String locale;

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public se.tink.backend.aggregation.rpc.UserProfile toAggregationUserProfile() {
        se.tink.backend.aggregation.rpc.UserProfile userProfile = new se.tink.backend.aggregation.rpc.UserProfile();

        userProfile.setLocale(this.locale);

        return userProfile;
    }
}

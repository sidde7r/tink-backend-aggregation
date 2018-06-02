package se.tink.backend.export.model;

import java.util.Date;
import java.util.List;
import se.tink.backend.export.model.submodels.ExportFacebookFriend;

public class FacebookDetails {

    private final String firstName;
    private final String lastName;
    private final Date birthday;
    private final String email;
    private final String locationName;
    private final String state;
    private final List<ExportFacebookFriend> friends;

    public FacebookDetails(String firstName, String lastName, Date birthday, String email,
            String locationName, String state, List<ExportFacebookFriend> friends) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
        this.email = email;
        this.locationName = locationName;
        this.state = state;
        this.friends = friends;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Date getBirthday() {
        return birthday;
    }

    public String getEmail() {
        return email;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getState() {
        return state;
    }

    public List<ExportFacebookFriend> getFriends() {
        return friends;
    }
}

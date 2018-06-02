package se.tink.backend.consent.core;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * User entity with the properties that are needed by the consent library / service. This library cannot use
 * se.tink.user.Core.User since that object is defined in main-api and consent-lib is used by main-api.
 */
public class User {
    private String id;
    private String username;
    private String locale;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void validate() {
        Preconditions.checkState(!Strings.isNullOrEmpty(id), "Id must not be null or empty.");
        Preconditions.checkState(!Strings.isNullOrEmpty(username), "UserName must not be null or empty.");
        Preconditions.checkState(!Strings.isNullOrEmpty(locale), "Locale must not be null or empty.");
    }
}

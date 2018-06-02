package se.tink.backend.export.model;

import java.util.Date;
import java.util.List;
import se.tink.backend.export.helper.DefaultSetter;
import se.tink.backend.export.model.submodels.ExportSubscription;
import se.tink.backend.export.model.submodels.ExportUserSettings;

/**
 * Includes data from User and UserProfile
 */
public class UserDetails implements DefaultSetter {

    private final String name;
    private final String nationalId;
    private final String gender;
    private final String market;
    private final String currency;
    private final String username;
    private final String created;
    private final String comment;
    private final String deleted;
    private final List<String> reasons;
    private final ExportUserSettings settings;
    private final List<ExportSubscription> subscriptions;

    public UserDetails(String name, String nationalId, String gender, String market, String currency,
            String username, Date created, String comment, Date deleted, List<String> reasons,
            ExportUserSettings settings, List<ExportSubscription> subscriptions) {
        this.name = name;
        this.nationalId = nationalId;
        this.gender = gender;
        this.market = market;
        this.currency = currency;
        this.username = username;
        this.created = notNull(created);
        this.comment = comment;
        this.deleted = notNull(deleted);
        this.reasons = reasons;
        this.settings = settings;
        this.subscriptions = subscriptions;
    }

    public String getName() {
        return name;
    }

    public String getNationalId() {
        return nationalId;
    }

    public String getGender() {
        return gender;
    }

    public String getMarket() {
        return market;
    }

    public String getCurrency() {
        return currency;
    }

    public String getUsername() {
        return username;
    }

    public String getCreated() {
        return created;
    }

    public String getComment() {
        return comment;
    }

    public String getDeleted() {
        return deleted;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public ExportUserSettings getSettings() {
        return settings;
    }

    public List<ExportSubscription> getSubscriptions() {
        return subscriptions;
    }
}

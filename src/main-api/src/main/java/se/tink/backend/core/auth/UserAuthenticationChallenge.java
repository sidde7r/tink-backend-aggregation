package se.tink.backend.core.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import org.joda.time.DateTime;
import org.joda.time.Period;
import se.tink.libraries.auth.ChallengeStatus;

@Entity
@Table(name = "users_authentication_challenges")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserAuthenticationChallenge {
    private static final Period CHALLENGE_LIFETIME = Period.minutes(5);

    @Id
    private String challenge;
    private String keyId;
    private String userId;
    private Date created;
    private Date expiry;
    @Enumerated(EnumType.STRING)
    private ChallengeStatus status;

    private UserAuthenticationChallenge() {

    }

    public String getChallenge() {
        return challenge;
    }

    public String getKeyId() {
        return keyId;
    }

    public String getUserId() {
        return userId;
    }

    public Date getCreated() {
        return created;
    }

    public Date getExpiry() {
        return expiry;
    }

    public ChallengeStatus getStatus() {
        return status;
    }

    public UserAuthenticationChallenge consume() {
        if (status.isValid() && DateTime.now().isAfter(new DateTime(expiry))) {
            status = ChallengeStatus.EXPIRED;
        } else if (status.isValid()) {
            status = ChallengeStatus.CONSUMED;
        } else {
            status = ChallengeStatus.INVALID;
        }

        return this;
    }

    @VisibleForTesting
    UserAuthenticationChallenge expire() {
        expiry = DateTime.now().minusMinutes(5).toDate();
        return this;
    }

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private String challenge;
        private String keyId;
        private String userId;

        private Builder() {

        }

        public Builder withChallenge(String challenge) {
            this.challenge = challenge;
            return this;
        }

        public Builder withKeyId(String keyId) {
            this.keyId = keyId;
            return this;
        }

        public Builder withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public UserAuthenticationChallenge build() {
            Preconditions.checkState(!Strings.isNullOrEmpty(challenge));
            Preconditions.checkState(!Strings.isNullOrEmpty(keyId));
            Preconditions.checkState(!Strings.isNullOrEmpty(userId));

            UserAuthenticationChallenge userAuthenticationChallenge = new UserAuthenticationChallenge();
            userAuthenticationChallenge.challenge = challenge;
            userAuthenticationChallenge.userId = userId;
            userAuthenticationChallenge.keyId = keyId;
            userAuthenticationChallenge.created = new Date();
            userAuthenticationChallenge.expiry = DateTime.now().plus(CHALLENGE_LIFETIME).toDate();
            userAuthenticationChallenge.status = ChallengeStatus.VALID;

            return userAuthenticationChallenge;
        }
    }
}

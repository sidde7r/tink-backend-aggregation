package se.tink.backend.core;

import java.io.Serializable;
import javax.persistence.IdClass;

@IdClass(UserOAuth2ClientPk.class)
public class UserOAuth2ClientPk implements Serializable {
    private static final long serialVersionUID = 1L;
    private String clientId;
    private String userId;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserOAuth2ClientPk that = (UserOAuth2ClientPk) o;

        if (!clientId.equals(that.clientId)) {
            return false;
        }
        return userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        int result = clientId.hashCode();
        result = 31 * result + userId.hashCode();
        return result;
    }
}

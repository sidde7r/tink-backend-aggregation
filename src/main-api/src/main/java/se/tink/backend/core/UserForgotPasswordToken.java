package se.tink.backend.core;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import se.tink.backend.utils.StringUtils;

@Entity
@Table(name = "users_forgot_password_tokens")
public class UserForgotPasswordToken {
    @Id
    private String id;
    private Date inserted;
    private String userId;

    public UserForgotPasswordToken() {
        id = StringUtils.generateUUID();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getInserted() {
        return inserted;
    }

    public void setInserted(Date inserted) {
        this.inserted = inserted;
    }
}

package se.tink.backend.core.auth.bankid;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "bankid_authentications")
public class BankIdAuthentication {
    private String nationalId;
    private String autostartToken;
    @Id
    private String id;
    @Enumerated(EnumType.STRING)
    private BankIdAuthenticationStatus status;
    private Date created;
    private Date updated;
    private String clientKey;
    private String oAuth2ClientId;

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getAutostartToken() {
        return autostartToken;
    }

    public void setAutostartToken(String autostartToken) {
        this.autostartToken = autostartToken;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BankIdAuthenticationStatus getStatus() {
        return status;
    }

    public void setStatus(BankIdAuthenticationStatus status) {
        this.status = status;
    }

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public String getOAuth2ClientId() {
        return oAuth2ClientId;
    }

    public void setOAuth2ClientId(String oAuth2ClientId) {
        this.oAuth2ClientId = oAuth2ClientId;
    }
}

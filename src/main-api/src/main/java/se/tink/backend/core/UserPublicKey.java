package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Type;
import se.tink.backend.core.auth.AuthenticationSource;
import se.tink.backend.core.auth.UserPublicKeyType;
import se.tink.backend.utils.StringUtils;

@Entity
@Table(name = "users_public_keys")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPublicKey {
    @Id
    @ApiModelProperty(name = "id", value="The internal identifier of the key.", example = "d929c82d2ee727ccbea9c50c669a71075249899f", required = true)
    private String id;
    @ApiModelProperty(name = "userId", value="UserID for the key's owner.", example = "f1d2d2f924e986ac86fdf7b36c94bcdf32beec15", required = true)
    private String userId;
    @ApiModelProperty(name = "deviceId", value="DeviceID for which the key is valid.", example = "98BC6C05-2FB3-4E52-9EBB-427622034B9B", required = true)
    private String deviceId;
    @ApiModelProperty(name = "active", value="Status for usage of the key.")
    private boolean active;
    @ApiModelProperty(name = "created", value="Date of key creation.")
    private Date created;
    @Type(type = "text")
    @ApiModelProperty(name = "publicKey", value="The key in PEM or PKCS#8 format.", required = true)
    private String publicKey;
    @Enumerated(EnumType.STRING)
    @ApiModelProperty(name = "authenticationSource", value="What type of authentication does the key provide.")
    private AuthenticationSource authenticationSource;
    @Enumerated(EnumType.STRING)
    @ApiModelProperty(name = "keyType", value="Key algorithm")
    private UserPublicKeyType type;

    public UserPublicKey() {
        this.id = StringUtils.generateUUID();
        this.created = new Date();
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public boolean isActive() {
        return active;
    }

    public Date getCreated() {
        return created;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public AuthenticationSource getAuthenticationSource() {
        return authenticationSource;
    }

    public UserPublicKeyType getType() {
        return type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setAuthenticationSource(AuthenticationSource authenticationSource) {
        this.authenticationSource = authenticationSource;
    }

    public void setType(UserPublicKeyType type) {
        this.type = type;
    }
}

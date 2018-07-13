package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.useractivation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Identity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PinSrpDataEntity {
    private String salt;
    private String username;
    private String verificator;

    @JsonIgnore
    public static PinSrpDataEntity create(Sparebank1Identity identity) {
        PinSrpDataEntity pinSrpDataEntity = new PinSrpDataEntity();

        pinSrpDataEntity.setSalt(String.valueOf(identity.getSalt()));
        pinSrpDataEntity.setUsername(identity.getUserName());
        pinSrpDataEntity.setVerificator(String.valueOf(identity.getVerificator()));

        return pinSrpDataEntity;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getVerificator() {
        return verificator;
    }

    public void setVerificator(String verificator) {
        this.verificator = verificator;
    }
}

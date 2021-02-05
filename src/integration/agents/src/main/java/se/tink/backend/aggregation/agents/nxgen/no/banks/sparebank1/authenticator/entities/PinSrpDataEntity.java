package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Identity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
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
}

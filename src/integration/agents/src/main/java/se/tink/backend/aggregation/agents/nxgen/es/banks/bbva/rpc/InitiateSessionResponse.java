package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.UserEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitiateSessionResponse extends BbvaResponse {
    private String name;
    private String surname;
    private UserEntity user;
    private String identificationTypeCode;

    public UserEntity getUser() {
        return user;
    }

    public String getIdentificationTypeCode() {
        return identificationTypeCode;
    }

    @JsonIgnore
    public String getName() {
        return String.format("%s %s", Strings.nullToEmpty(name), Strings.nullToEmpty(surname));
    }
}

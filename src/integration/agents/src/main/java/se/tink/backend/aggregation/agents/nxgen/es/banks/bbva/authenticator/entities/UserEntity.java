package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.entities;

import java.util.Map;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserEntity {
    private String id;
    private String authenticationType;
    private String alias;
    private String role;
    private Map<String, String> otherUserInfo;
    private PersonalizationEntity personalization;
    private PersonEntity person;
    private String lastAccessDate;

    public String getId() {
        return id;
    }
}

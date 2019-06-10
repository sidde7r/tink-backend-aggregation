package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateAccountEntity {
    private String email;
    private String password;
    private FlagsEntity flags;

    public CreateAccountEntity() {}

    public CreateAccountEntity(String email, String password, FlagsEntity flags) {
        this.email = email;
        this.password = password;
        this.flags = flags;
    }
}

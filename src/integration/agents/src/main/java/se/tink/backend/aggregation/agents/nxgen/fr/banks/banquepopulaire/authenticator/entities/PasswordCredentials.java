package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PasswordCredentials {
    private String password;
    private String id;
    private String login;
    private String type;

    public PasswordCredentials(String password, String id, String login, String type) {
        this.password = password;
        this.id = id;
        this.login = login;
        this.type = type;
    }

    public static PasswordCredentials create(
            String password, String id, String login, String type) {
        return new PasswordCredentials(password, id, login, type);
    }
}

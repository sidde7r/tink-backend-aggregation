package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

public class InitBankIdModuleInput extends BankIdModuleInput {
    public static final String OPERATION = "initAuth";

    private String user;
    private String token;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public InitBankIdModuleInput(String user) {
        super(OPERATION);
        this.token = "";
        this.user = user;
    }
}

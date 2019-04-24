package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.entities;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginEntity {
    private String customerId;
    private List<AccountEntity> accounts;
    private String loginPath;
    private String username;

    public String getCustomerId() {
        return customerId;
    }

    public List<AccountEntity> getAccounts() {
        return Optional.ofNullable(accounts).orElseGet(Collections::emptyList);
    }

    public String getLoginPath() {
        return loginPath;
    }

    public String getUsername() {
        return username;
    }
}

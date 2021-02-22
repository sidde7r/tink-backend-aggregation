package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;

public class BankinterHolder {
    private final String xPath;
    private final Role role;

    public BankinterHolder(String xPath, Role role) {
        this.xPath = xPath;
        this.role = role;
    }

    public String getxPath() {
        return xPath;
    }

    public Role getRole() {
        return role;
    }
}

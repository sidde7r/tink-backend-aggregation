package se.tink.backend.export.model;

import java.util.List;
import se.tink.backend.export.model.submodels.ExportAccount;

public class Accounts {

    private final List<ExportAccount> accounts;

    public Accounts(List<ExportAccount> accounts) {
        this.accounts = accounts;
    }

    public List<ExportAccount> getAccounts() {
        return accounts;
    }
}

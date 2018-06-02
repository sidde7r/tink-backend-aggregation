package se.tink.backend.export.model.submodels;

import java.util.Date;
import se.tink.backend.export.helper.DefaultSetter;

public class ExportAccount implements DefaultSetter {


    private final String accountNumber;
    private final String name;
    private final String type;
    private final String updated;
    private final String availableCredit;
    private final String balance;
    private final String closed;
    private final String excluded;
    private final String favored;
    private final String ownership;

    public ExportAccount(String accountNumber, String name, String type, Date updated, Double availableCredit,
            Double balance, String closed, String excluded, String favored, Double ownership) {
        this.accountNumber = notNull(accountNumber);
        this.name = notNull(name);
        this.type = notNull(type);
        this.updated = notNull(updated);
        this.availableCredit = notNull(availableCredit);
        this.balance = notNull(balance);
        this.closed = notNull(closed);
        this.excluded = notNull(excluded);
        this.favored = notNull(favored);
        this.ownership = notNull(ownership);
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getUpdated() {
        return updated;
    }

    public String getAvailableCredit() {
        return availableCredit;
    }

    public String getBalance() {
        return balance;
    }

    public String getClosed() {
        return closed;
    }

    public String getExcluded() {
        return excluded;
    }

    public String getFavored() {
        return favored;
    }

    public String getOwnership() {
        return ownership;
    }
}

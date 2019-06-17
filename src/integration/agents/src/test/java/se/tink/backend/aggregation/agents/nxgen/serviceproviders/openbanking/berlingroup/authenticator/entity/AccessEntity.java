package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {

    private final List<IbanEntity> accounts = new ArrayList<>();
    private final List<IbanEntity> transactions = new ArrayList<>();
    private final List<IbanEntity> balances = new ArrayList<>();
    private String availableAccounts;
    private String allPsd2;

    private AccessEntity() {}

    private AccessEntity(String availableAccounts, String allPsd2) {
        this.availableAccounts = availableAccounts;
        this.allPsd2 = allPsd2;
    }

    public static AccessEntityBuilder builder() {
        return new AccessEntityBuilder();
    }

    public void addIban(final String iban) {
        accounts.add(new IbanEntity(iban));
        transactions.add(new IbanEntity(iban));
        balances.add(new IbanEntity(iban));
    }

    public void addIbans(final List<String> ibans) {
        ibans.forEach(this::addIban);
    }

    public static class AccessEntityBuilder {

        private String availableAccounts;
        private String allPsd2;

        AccessEntityBuilder() {}

        public AccessEntityBuilder availableAccounts(String availableAccounts) {
            this.availableAccounts = availableAccounts;
            return this;
        }

        public AccessEntityBuilder allPsd2(String allPsd2) {
            this.allPsd2 = allPsd2;
            return this;
        }

        public AccessEntity build() {
            return new AccessEntity(availableAccounts, allPsd2);
        }
    }
}

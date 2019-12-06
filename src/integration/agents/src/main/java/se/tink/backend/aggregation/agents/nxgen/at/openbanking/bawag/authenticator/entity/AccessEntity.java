package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AccessEntity {

    protected List<IbanEntity> accounts = new ArrayList<>();
    protected List<IbanEntity> transactions = new ArrayList<>();
    protected List<IbanEntity> balances = new ArrayList<>();
    protected String allPsd2;

    @JsonIgnore
    private AccessEntity(AccessEntity.Builder builder) {
        accounts = builder.accounts;
        transactions = builder.transactions;
        balances = builder.balances;
        allPsd2 = builder.allPsd2;
    }

    public static class Builder {
        private List<IbanEntity> accounts = new ArrayList<>();
        private List<IbanEntity> transactions = new ArrayList<>();
        private List<IbanEntity> balances = new ArrayList<>();
        private String allPsd2;

        public AccessEntity.Builder withAccounts(List<String> accounts) {
            this.accounts =
                    accounts.stream()
                            .map(iban -> new IbanEntity(iban))
                            .collect(Collectors.toList());
            return this;
        }

        public AccessEntity.Builder withTransactions(List<String> transactions) {
            this.transactions =
                    transactions.stream()
                            .map(iban -> new IbanEntity(iban))
                            .collect(Collectors.toList());
            return this;
        }

        public AccessEntity.Builder withBalances(List<String> balances) {
            this.balances =
                    balances.stream()
                            .map(iban -> new IbanEntity(iban))
                            .collect(Collectors.toList());
            return this;
        }

        public AccessEntity.Builder withAllPsd2(String allPsd2) {
            this.allPsd2 = allPsd2;
            return this;
        }

        public AccessEntity.Builder addIban(String iban) {
            IbanEntity ibanEntity = new IbanEntity(iban);
            this.accounts.add(ibanEntity);
            this.transactions.add(ibanEntity);
            this.balances.add(ibanEntity);
            return this;
        }

        public AccessEntity.Builder addIbans(List<String> ibans) {
            ibans.forEach(this::addIban);
            return this;
        }

        public AccessEntity build() {
            return new AccessEntity(this);
        }
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class AccessEntity {

    protected List<IbanEntity> accounts;
    protected List<IbanEntity> transactions;
    protected List<IbanEntity> balances;
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
            this.accounts = accounts.stream().map(IbanEntity::new).collect(Collectors.toList());
            return this;
        }

        public AccessEntity.Builder withAccountsList(List<IbanEntity> accounts) {
            this.accounts = accounts;
            return this;
        }

        public AccessEntity.Builder withTransactions(List<String> transactions) {
            this.transactions =
                    transactions.stream().map(IbanEntity::new).collect(Collectors.toList());
            return this;
        }

        public AccessEntity.Builder withBalances(List<String> balances) {
            this.balances = balances.stream().map(IbanEntity::new).collect(Collectors.toList());
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

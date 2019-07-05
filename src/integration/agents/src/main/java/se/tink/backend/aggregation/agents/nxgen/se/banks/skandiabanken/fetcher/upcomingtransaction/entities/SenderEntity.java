package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
class SenderEntity {
    @JsonProperty("Address")
    private AddressEntity address;

    @JsonProperty("BankAccount")
    private BankAccountEntity bankAccount;

    @JsonProperty("BankAccountTransactionNote")
    private String bankAccountTransactionNote;

    @JsonProperty("Name")
    private String name;

    public BankAccountEntity getBankAccount() {
        return bankAccount;
    }

    public String getBankAccountTransactionNote() {
        return bankAccountTransactionNote;
    }

    public String getName() {
        return name;
    }
}

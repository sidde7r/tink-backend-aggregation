package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.transactionalaccount.entities.BankAccountsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class FetchAccountResponse {
    @JsonProperty("BankAccounts")
    private List<BankAccountsEntity> bankAccounts;

    @JsonProperty("Type")
    private int type;

    @JsonProperty("TypeName")
    private String typeName = "";

    @JsonProperty("NationalIdentificationNumber")
    private String nationalIdentificationNumber = "";

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setAccounts(List<BankAccountsEntity> bankAccounts) {
        this.bankAccounts = bankAccounts;
    }

    @JsonIgnore
    public List<BankAccountsEntity> getAccounts() {
        return bankAccounts;
    }

    @JsonIgnore
    public List<TransactionalAccount> toTinkAccount() {
        return getAccounts().stream()
                .filter(BankAccountsEntity::isTransactionalAccount)
                .map(BankAccountsEntity::toTinkTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}

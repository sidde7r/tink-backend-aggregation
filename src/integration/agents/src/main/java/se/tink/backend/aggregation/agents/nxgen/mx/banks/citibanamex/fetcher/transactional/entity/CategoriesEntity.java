package se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class CategoriesEntity {

    private List<AccountInfoEntity> accountInfo;
    private String id;

    @JsonProperty("name")
    private String accountName;

    private String shortName;

    public List<TransactionalAccount> toTransactionalAccounts(String holderName) {
        return accountInfo.stream()
                .filter(AccountInfoEntity::getEnabled)
                .filter(AccountInfoEntity::isTransactionalAccount)
                .map(account -> account.toTinkAccount(accountName, holderName))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}

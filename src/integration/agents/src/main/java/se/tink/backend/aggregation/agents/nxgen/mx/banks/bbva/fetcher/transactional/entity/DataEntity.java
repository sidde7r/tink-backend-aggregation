package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.entity;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class DataEntity {
    private List<ContractsItemEntity> contracts;
    private List<FamilyItemEntity> family;

    public Collection<TransactionalAccount> toTransactionalAccounts(String holderName) {
        return contracts
                .stream()
                .filter(ContractsItemEntity::isValid)
                .map(x -> x.toTransactionalAccount(holderName))
                .collect(Collectors.toList());
    }
}

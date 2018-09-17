package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

@JsonObject
public class ProductListEntity extends ArrayList<ProductEntity> {

    public List<TransactionalAccount> toTransactionalAccounts() {
        return this.stream().map(ProductEntity::toTransactionalAccount).collect(Collectors.toList());
    }

}

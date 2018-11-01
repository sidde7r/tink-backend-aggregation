package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.entity.ProductEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

@JsonObject
public class AccountResponse {

    @JsonProperty("products")
    private List<ProductEntity> productListEntity;

    public List<TransactionalAccount> toTransactionalAccounts() {
        return productListEntity.stream()
                .filter(productEntity -> productEntity.isValid())
                .map(ProductEntity::toTransactionalAccount).collect(Collectors.toList());
    }

}

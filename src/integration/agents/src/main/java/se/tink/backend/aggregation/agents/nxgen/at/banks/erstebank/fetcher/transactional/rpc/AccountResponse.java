package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.entity.ProductEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountResponse {

    @JsonProperty("products")
    private List<ProductEntity> productListEntity;

    public List<TransactionalAccount> toTransactionalAccounts() {
        return productListEntity.stream()
                .filter(productEntity -> productEntity.isValidTransactional())
                .map(ProductEntity::toTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<CreditCardAccount> toCreditCardAccounts() {
        return productListEntity.stream()
                .filter(
                        productEntity ->
                                productEntity.isCreditCardAccount()
                                        && productEntity.isValidCreditCardAccount())
                .map(ProductEntity::toCreditCardAccount)
                .collect(Collectors.toList());
    }
}

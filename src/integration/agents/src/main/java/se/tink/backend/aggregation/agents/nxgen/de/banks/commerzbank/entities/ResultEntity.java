package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities.ItemsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class ResultEntity {
    private Object metaData;
    private List<ItemsEntity> items;

    public List<ItemsEntity> getItems() {
        return items;
    }

    public Object getMetaData() {
        return metaData;
    }

    public Collection<CreditCardAccount> toCreditAccounts() {
        Collection<CreditCardAccount> result = new ArrayList<>();
        for (ItemsEntity itemEntity : items) {
            result.addAll(
                    itemEntity.getProducts().stream()
                            .filter(
                                    productsEntity ->
                                            productsEntity.hasValidProductId()
                                                    && productsEntity.isCreditCard())
                            .map(x -> x.toCreditCardAccount(itemEntity))
                            .collect(Collectors.toList()));
        }
        return result;
    }

    public Collection<TransactionalAccount> toTransactionalAccounts() {
        Collection<TransactionalAccount> result = new ArrayList<>();
        for (ItemsEntity itemEntity : items) {
            result.addAll(
                    itemEntity.getProducts().stream()
                            .filter(
                                    product ->
                                            product.hasValidProductId() && !product.isCreditCard())
                            .map(product -> product.toTransactionalAccount(itemEntity))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList()));
        }
        return result;
    }
}

package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
        return items.stream()
                .flatMap(this::mapItemToCreditCardsStream)
                .collect(Collectors.toList());
    }

    private Stream<CreditCardAccount> mapItemToCreditCardsStream(ItemsEntity itemsEntity) {
        return itemsEntity.getProducts().stream()
                .filter(
                        productsEntity ->
                                productsEntity.hasValidProductId() && productsEntity.isCreditCard())
                .map(productsEntity -> productsEntity.toCreditCardAccount(itemsEntity));
    }

    public Collection<TransactionalAccount> toTransactionalAccounts() {
        return items.stream()
                .flatMap(this::mapItemToTransactionalAccountsStream)
                .collect(Collectors.toList());
    }

    private Stream<TransactionalAccount> mapItemToTransactionalAccountsStream(
            ItemsEntity itemsEntity) {
        return itemsEntity.getProducts().stream()
                .filter(product -> product.hasValidProductId() && !product.isCreditCard())
                .map(product -> product.toTransactionalAccount(itemsEntity))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}

package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities.ItemsEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities.ProductsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

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


    public Collection<CreditCardAccount> toCreditAccounts(){
        Collection<CreditCardAccount> result = new ArrayList<>();
        for (ItemsEntity entity : items) {
            result.addAll(entity.getProducts().stream()
                    .filter(productsEntity -> productsEntity.hasValidProductId() && productsEntity.isCreditCard())
                    .map(ProductsEntity::toCreditCardAccount).collect(Collectors.toList()));
        }
        return result;
    }

    public Collection<TransactionalAccount> toTransactionalAccounts(){
        Collection<TransactionalAccount> result = new ArrayList<>();
        for (ItemsEntity entity : items) {
            result.addAll(entity.getProducts().stream()
                    .filter(productsEntity -> productsEntity.hasValidProductId() && !productsEntity.isCreditCard())
                    .map(ProductsEntity::toTransactionalAccount).collect(Collectors.toList()));
        }
        return result;
    }


}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.ProductV31Entity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@Getter
public class ProductV31Response extends BaseV31Response<List<ProductV31Entity>> {

    public String getAccountProductType(String accountId) {
        return getData()
                .flatMap(
                        entities ->
                                entities.stream()
                                        .filter(entity -> entity.getAccountId().equals(accountId))
                                        .findFirst())
                .map(ProductV31Entity::getProductType)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Mandatory field called 'ProductType' for specific account was not found."));
    }
}

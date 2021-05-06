package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.entities;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GlobalPositionDto {
    private List<ProductEntity> products;

    public List<ProductEntity> getProducts() {
        return Optional.ofNullable(products).orElseGet(Collections::emptyList);
    }
}

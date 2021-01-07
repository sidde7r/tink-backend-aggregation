package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity.IngProduct;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProductsResponse {

    private List<IngProduct> products;

    public List<IngProduct> getProducts() {
        return products;
    }

    @JsonIgnore
    public static ProductsResponse create(List<IngProduct> products) {
        ProductsResponse productsResponse = new ProductsResponse();
        productsResponse.products = products;
        return productsResponse;
    }
}

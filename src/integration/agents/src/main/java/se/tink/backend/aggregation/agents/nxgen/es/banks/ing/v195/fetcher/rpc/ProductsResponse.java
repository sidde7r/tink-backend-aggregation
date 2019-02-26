package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity.Product;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class ProductsResponse {

    private List<Product> products;

    public List<Product> getProducts() {
        return products;
    }

    @JsonIgnore
    public static ProductsResponse create(List<Product> products) {
        ProductsResponse productsResponse = new ProductsResponse();
        productsResponse.products = products;
        return productsResponse;
    }
}

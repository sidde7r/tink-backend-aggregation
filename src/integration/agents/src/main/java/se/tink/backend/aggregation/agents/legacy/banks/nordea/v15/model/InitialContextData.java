package se.tink.backend.aggregation.agents.banks.nordea.v15.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InitialContextData {
    private static final TypeReference<List<ProductEntity>> LIST_TYPE_REFERENCE =
            new TypeReference<List<ProductEntity>>() {};

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonProperty("product")
    private List<ProductEntity> products;

    public List<ProductEntity> getProducts() {
        return products;
    }

    /**
     * Nordea API is a bit weird and send items on different formats depending on the number of
     * items. Multiple rows means that we will get an List of items and one row will not be typed as
     * an array.
     */
    public void setProduct(Object input) {

        if (input instanceof Map) {
            products = Lists.newArrayList(MAPPER.convertValue(input, ProductEntity.class));
        } else {
            products = MAPPER.convertValue(input, LIST_TYPE_REFERENCE);
        }
    }
}

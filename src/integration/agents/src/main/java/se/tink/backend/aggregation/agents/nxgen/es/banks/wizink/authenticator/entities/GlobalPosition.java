package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.entities.ProductEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GlobalPosition {

    @JsonProperty("fechaNacimiento")
    private String encodedDateOfBirth;

    private List<CardEntity> cards;
    private List<ProductEntity> products;

    public List<CardEntity> getCards() {
        return cards;
    }

    public List<ProductEntity> getProducts() {
        return products;
    }

    public String getEncodedDateOfBirth() {
        return encodedDateOfBirth;
    }
}

package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GlobalPosition {
    private List<CardEntity> cards;
    private List<ProductEntity> products;

    public List<CardEntity> getCards() {
        return cards;
    }

    public List<ProductEntity> getProducts() {
        return products;
    }
}

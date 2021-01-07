package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class IngDetectedCategory {

    private int categoryId;
    private double score;

    public int getCategoryId() {
        return categoryId;
    }

    public double getScore() {
        return score;
    }
}

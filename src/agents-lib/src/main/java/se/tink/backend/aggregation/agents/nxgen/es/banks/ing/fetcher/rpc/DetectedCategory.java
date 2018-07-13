package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class DetectedCategory {

    private int categoryId;
    private double score;

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}

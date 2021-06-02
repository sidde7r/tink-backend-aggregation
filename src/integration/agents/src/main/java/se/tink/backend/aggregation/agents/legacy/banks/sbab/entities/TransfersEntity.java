package se.tink.backend.aggregation.agents.banks.sbab.entities;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransfersEntity {
    private List<TransactionEntity> completed = new ArrayList<>();
    private List<TransactionEntity> upcoming = new ArrayList<>();

    public List<TransactionEntity> getCompleted() {
        return completed;
    }

    public List<TransactionEntity> getUpcoming() {
        return upcoming;
    }
}

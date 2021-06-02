package se.tink.backend.aggregation.agents.banks.sbab.entities;

import java.util.ArrayList;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransfersEntity {
    private ArrayList<TransactionEntity> completed = new ArrayList<>();
    private ArrayList<TransactionEntity> upcoming = new ArrayList<>();

    public ArrayList<TransactionEntity> getCompleted() {
        return completed;
    }

    public ArrayList<TransactionEntity> getUpcoming() {
        return upcoming;
    }
}

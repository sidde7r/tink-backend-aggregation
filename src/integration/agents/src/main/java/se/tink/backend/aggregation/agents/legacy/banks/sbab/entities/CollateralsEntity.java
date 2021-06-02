package se.tink.backend.aggregation.agents.banks.sbab.entities;

import java.util.ArrayList;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CollateralsEntity {

    private ArrayList<LoanEntity> mortgages = new ArrayList<>();

    public ArrayList<LoanEntity> getMortgages() {
        return mortgages;
    }
}

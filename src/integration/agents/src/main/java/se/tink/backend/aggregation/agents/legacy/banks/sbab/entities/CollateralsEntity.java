package se.tink.backend.aggregation.agents.banks.sbab.entities;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CollateralsEntity {

    private List<LoanEntity> mortgages = new ArrayList<>();

    public List<LoanEntity> getMortgages() {
        return mortgages;
    }
}

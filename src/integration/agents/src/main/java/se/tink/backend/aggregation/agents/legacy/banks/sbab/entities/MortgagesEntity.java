package se.tink.backend.aggregation.agents.banks.sbab.entities;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MortgagesEntity {

    private List<CollateralsEntity> collaterals = new ArrayList<>();

    public List<CollateralsEntity> getCollaterals() {
        return collaterals;
    }
}

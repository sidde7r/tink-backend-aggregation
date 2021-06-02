package se.tink.backend.aggregation.agents.banks.sbab.entities;

import java.util.ArrayList;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MortgagesEntity {

    private ArrayList<CollateralsEntity> collaterals = new ArrayList<>();

    public ArrayList<CollateralsEntity> getCollaterals() {
        return collaterals;
    }
}

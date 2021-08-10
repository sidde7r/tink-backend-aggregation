package se.tink.backend.aggregation.agents.banks.sbab.entities;

import java.math.BigInteger;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BlancoEntity {
    private BigInteger totalAmount;
    private List<BlancosEntity> list;

    public List<BlancosEntity> getList() {
        return list;
    }
}

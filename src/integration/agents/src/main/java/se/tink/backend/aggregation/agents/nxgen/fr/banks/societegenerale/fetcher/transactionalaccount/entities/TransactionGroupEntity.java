package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.fetcher.transactionalaccount.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionGroupEntity {

    private int nbTotalOcc;
    private List<TransactionEntity> elements;

    public List<TransactionEntity> getElements() {
        return elements;
    }

}

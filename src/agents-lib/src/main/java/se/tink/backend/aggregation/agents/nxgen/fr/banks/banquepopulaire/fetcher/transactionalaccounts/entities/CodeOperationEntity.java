package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.transactionalaccounts.entities;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities.TypeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CodeOperationEntity extends TypeEntity {
    private String sensOperation;

    public String getSensOperation() {
        return sensOperation;
    }
}

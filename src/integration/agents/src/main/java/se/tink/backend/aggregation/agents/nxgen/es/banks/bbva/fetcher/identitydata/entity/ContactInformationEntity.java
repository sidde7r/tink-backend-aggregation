package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.entity;

import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.TypeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContactInformationEntity {
    private TypeEntity type;
    private String name;
    private boolean checked;
}

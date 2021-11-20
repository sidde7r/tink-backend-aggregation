package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.entity;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class NickelOperationCategory {

    private String i18nKey;
    private String id;
    private String label;
}

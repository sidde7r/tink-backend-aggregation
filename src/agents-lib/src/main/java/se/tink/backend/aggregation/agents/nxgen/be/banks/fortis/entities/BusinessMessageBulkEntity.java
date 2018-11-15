package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BusinessMessageBulkEntity {
    private List<MessagesEntity> messages;
    private String pewCode;
    private String globalIndicator;
    private String text;
}

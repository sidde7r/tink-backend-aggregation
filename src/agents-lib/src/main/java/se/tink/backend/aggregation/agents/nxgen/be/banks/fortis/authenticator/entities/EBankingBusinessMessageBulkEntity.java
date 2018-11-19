package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EBankingBusinessMessageBulkEntity {
    private Object globalIndicator;
    private List<Object> messages;
    private Object text;
    private Object pewCode;
}

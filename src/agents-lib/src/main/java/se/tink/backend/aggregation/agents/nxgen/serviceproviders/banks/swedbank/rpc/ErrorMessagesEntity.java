package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorMessagesEntity {
    private List<GeneralEntity> general;
    private List<FieldsEntity> fields;

    public List<GeneralEntity> getGeneral() {
        return general;
    }
    public List<FieldsEntity> getFields() {
        return fields;
    }
}

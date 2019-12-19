package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorMessagesEntity {
    private List<ErrorDetailsEntity> general;
    private List<FieldsEntity> fields;

    public List<ErrorDetailsEntity> getGeneral() {
        return general;
    }

    public List<FieldsEntity> getFields() {
        return fields;
    }
}

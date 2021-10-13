package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ErrorMessagesEntity {
    private List<ErrorDetailsEntity> general;
    private List<FieldsEntity> fields;
}

package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.entities.FieldsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class PaymentInitSignResponse {

    private String errorCode;
    private String errorMessage;
    private List<FieldsEntity> fields;
    private int statusCode;
    private String statusMessage;

    public Optional<String> getSignReference() {
        return fields.stream()
                .filter(FieldsEntity::isSignReference)
                .map(FieldsEntity::getMessage)
                .findFirst();
    }
}

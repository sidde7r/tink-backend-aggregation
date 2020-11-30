package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.transaction;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class TransactionExceptionEntity {

    private String code;
    private String message;
    private List<TransactionExceptionErrorEntity> errors;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<TransactionExceptionErrorEntity> getErrors() {
        return errors;
    }
}

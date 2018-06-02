package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ImmutableMap;
import se.tink.backend.core.transfer.SignableOperationStatuses;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorMessage {
    protected String code;
    protected String field;
    protected String message;
    protected String refId;

    private static final ImmutableMap<String, SignableOperationStatuses> SIGNABLE_STATUSES_BY_ERROR_CODE = ImmutableMap
            // "Betalningen kunde inte genomföras för att det inte fanns tillräckligt med pengar på kontot."
            .of("INSUFFICIENT_FUNDS", SignableOperationStatuses.CANCELLED);

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    /**
     * This method can be used when the ErrorMessage has to do with a SignableOperation. It gets the correct
     * SignableOperation status corresponding to the code field in this object. If no corresponding code, it returns
     * the more general error code FAILED.
     */
    @JsonIgnore
    public SignableOperationStatuses getSignableOperationStatus() {
        return SIGNABLE_STATUSES_BY_ERROR_CODE.containsKey(code) ?
                SIGNABLE_STATUSES_BY_ERROR_CODE.get(code) : SignableOperationStatuses.FAILED;
    }

}

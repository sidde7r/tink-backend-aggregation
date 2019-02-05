package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    private String errorMessage;
    private String code;
    private String errorMessageTitle;
    private String errorMessageDetail;
    private String errorCode;
    private String severity;
    private String labelCta;
    private String operationCta;
    private String clickToCall;
    private String evento;
    private String nombreOperativa;
    private String idCanal;

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getCode() {
        return code;
    }

    public String getErrorMessageTitle() {
        return errorMessageTitle;
    }

    public String getErrorMessageDetail() {
        return errorMessageDetail;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getSeverity() {
        return severity;
    }

    public String getLabelCta() {
        return labelCta;
    }

    public String getOperationCta() {
        return operationCta;
    }

    public String getClickToCall() {
        return clickToCall;
    }

    public String getEvento() {
        return evento;
    }

    public String getNombreOperativa() {
        return nombreOperativa;
    }

    public String getIdCanal() {
        return idCanal;
    }
}

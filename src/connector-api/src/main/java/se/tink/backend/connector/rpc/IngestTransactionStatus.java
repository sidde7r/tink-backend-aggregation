package se.tink.backend.connector.rpc;

public class IngestTransactionStatus {
    private String entityId;
    private int httpStatus;
    private Object response;

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public static IngestTransactionStatus create(String entityId, int httpStatus, Object response) {
        IngestTransactionStatus status = new IngestTransactionStatus();
        status.setEntityId(entityId);
        status.setHttpStatus(httpStatus);
        status.setResponse(response);
        return status;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
}

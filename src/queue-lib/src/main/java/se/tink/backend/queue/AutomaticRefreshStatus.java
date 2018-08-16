package se.tink.backend.queue;

public enum AutomaticRefreshStatus {
    NOT_INITIALIZED, RUNNING, FAILED, SUCCESS;

    private String error = "";

    public void setError(String errorCode){
        this.error = errorCode;
    }

    public String getError(String error){
        return error;
    }

}

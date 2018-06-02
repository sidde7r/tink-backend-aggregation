package se.tink.backend.export.validators.exception;

public class DataExportException extends RuntimeException {
    public DataExportException(Exception e){
        super(e);
    }

    public DataExportException(String message){
        super(message);
    }

    public DataExportException(String message, Throwable e) {
        super(message, e);
    }
}

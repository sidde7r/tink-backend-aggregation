package se.tink.backend.aggregation.agents.banks.norwegian.model;

public class ErrorEntity {
    private String code;
    private String message;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isBankIdAlreadyInProgress() {
        return "ALREADY_IN_PROGRESS".equalsIgnoreCase(code);
    }
}


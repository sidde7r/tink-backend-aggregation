package se.tink.backend.aggregation.agents.creditcards.ikano.api.errors;

public class ResponseError {
    private Type type;
    private String code;
    private String message;

    public Type getType() {
        return type;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setCode(String code) {
        this.code = code;
        this.type = ErrorProvider.getTypeOf(code);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public enum Type {
        USER_ERROR, FATAL_ERROR
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorEntity {
    private String code;
    private String message;
    private String id;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("code: ")
                .append(code)
                .append("\n")
                .append("message: ")
                .append(message)
                .append("\n")
                .append("id: ")
                .append(id)
                .append("\n");
        return sb.toString();
    }
}

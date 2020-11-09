package src.integration.bankid.rpc;

import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Getter
@JsonObject
@RequiredArgsConstructor
public class BankIdJsResponse {

    private String code;
    private String state;
    private String error;

    public static BankIdJsResponse fromQueryParametersMap(Map<String, String> queryStringParams) {
        return SerializationUtils.deserializeFromString(
                queryStringParams.get("data"), BankIdJsResponse.class);
    }

    public boolean hasError() {
        return error != null;
    }

    public void throwBankIdError() {
        if ("Authentication was cancelled.".equals(error)) {
            throw BankIdError.CANCELLED.exception();
        }
        throw BankIdError.UNKNOWN.exception("Received error: " + error);
    }
}

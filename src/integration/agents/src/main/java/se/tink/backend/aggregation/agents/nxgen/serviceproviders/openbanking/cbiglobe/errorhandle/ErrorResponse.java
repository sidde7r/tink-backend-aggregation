package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.errorhandle;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@JsonObject
public class ErrorResponse {
    private List<ErrorTppMessage> tppMessages;

    private String transactionStatus;

    public List<ErrorTppMessage> getTppMessages() {
        return tppMessages == null ? Collections.emptyList() : tppMessages;
    }

    public boolean tppMessagesContainsError(String code, String text) {
        if (tppMessages == null) {
            return false;
        } else {
            return tppMessages.stream()
                    .anyMatch(errorTppMessage -> errorTppMessage.isSameError(code, text));
        }
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public static ErrorResponse createFrom(HttpResponse httpResponse) {
        try {
            return httpResponse.getBody(ErrorResponse.class);
        } catch (RuntimeException e) {
            return null;
        }
    }
}

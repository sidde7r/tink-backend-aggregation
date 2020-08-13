package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class RequestDataEntity {

    private String otp;
    private SignContentDataEntity signContentData;
    private Boolean userCancelled;

    public RequestDataEntity(String cardNumber, String cardReaderResponse) {
        this.otp =
                String.format(
                        "{\"axaCardNumber\":\"%s\",\"response\":\"%s\"}",
                        cardNumber, cardReaderResponse);
    }

    public RequestDataEntity(SignContentDataEntity signContentData) {
        this.signContentData = signContentData;
        this.userCancelled = true;
    }
}

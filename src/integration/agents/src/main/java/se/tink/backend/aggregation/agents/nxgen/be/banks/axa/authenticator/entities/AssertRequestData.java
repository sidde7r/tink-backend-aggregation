package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AssertRequestData {

    private String action;

    @JsonProperty("assert")
    private String assertField;

    private String assertionId;
    private String fch;
    private InputEntity input;
    private RequestDataEntity data;
    private String method;
    private PublicKeyEntity publicKey;

    private AssertRequestData() {}

    private AssertRequestData withAction(String action) {
        this.action = action;
        return this;
    }

    private AssertRequestData withAssertField(String assertField) {
        this.assertField = assertField;
        return this;
    }

    private AssertRequestData withAssertionId(String assertionId) {
        this.assertionId = assertionId;
        return this;
    }

    private AssertRequestData withFch(String fch) {
        this.fch = fch;
        return this;
    }

    private AssertRequestData withInput(InputEntity input) {
        this.input = input;
        return this;
    }

    private AssertRequestData withData(RequestDataEntity data) {
        this.data = data;
        return this;
    }

    private AssertRequestData withMethod(String method) {
        this.method = method;
        return this;
    }

    private AssertRequestData withPublicKey(PublicKeyEntity publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public static AssertRequestData createUCRAssertData(String assertionId, String fch) {
        return new AssertRequestData()
                .withAction("form")
                .withAssertField("action")
                .withAssertionId(assertionId)
                .withFch(fch)
                .withInput(new InputEntity().setAuthenticator("UCR"));
    }

    public static AssertRequestData createCardNumberAssertData(
            String assertionId, String fch, String cardNumber) {
        return new AssertRequestData()
                .withAction("form")
                .withAssertField("action")
                .withAssertionId(assertionId)
                .withFch(fch)
                .withInput(new InputEntity().setCardNbr(cardNumber));
    }

    public static AssertRequestData createOtpAssertData(
            String assertionId, String fch, String cardNumber, String otp) {
        return new AssertRequestData()
                .withAction("authentication")
                .withAssertField("authenticate")
                .withAssertionId(assertionId)
                .withData(new RequestDataEntity(cardNumber, otp))
                .withFch(fch)
                .withMethod("otp");
    }

    public static AssertRequestData createProfileNameAssertData(
            String assertionId, String fch, String firstName) {
        return new AssertRequestData()
                .withAction("form")
                .withAssertField("action")
                .withAssertionId(assertionId)
                .withFch(fch)
                .withInput(new InputEntity().setProfileName(firstName));
    }

    public static AssertRequestData createAssertRegistrationRequest(
            String assertionId, String fch, String ecRawPublicKey) {
        return new AssertRequestData()
                .withAction("registration")
                .withAssertField("register")
                .withAssertionId(assertionId)
                .withFch(fch)
                .withMethod("pin")
                .withPublicKey(new PublicKeyEntity(ecRawPublicKey, "ecraw"));
    }

    public static AssertRequestData createAssertConfirmationData(
            String assertionId, String fch, String payload, String signedPayload) {
        return new AssertRequestData()
                .withAction("confirmation")
                .withAssertField("action")
                .withAssertionId(assertionId)
                .withData(new RequestDataEntity(new SignContentDataEntity(payload, signedPayload)))
                .withFch(fch);
    }

    public static AssertRequestData createAssertAuthenticationData(
            String assertionId, String signedFch) {
        return new AssertRequestData()
                .withAction("authentication")
                .withAssertField("authenticate")
                .withAssertionId(assertionId)
                .withFch(signedFch)
                .withMethod("pin");
    }
}

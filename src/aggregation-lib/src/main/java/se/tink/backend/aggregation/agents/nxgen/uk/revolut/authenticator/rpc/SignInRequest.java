package se.tink.backend.aggregation.agents.nxgen.uk.revolut.authenticator.rpc;

import java.security.SecureRandom;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignInRequest {
    private String phone;
    private String password;

    public static SignInRequest build(String phoneNumber) {
        return new SignInRequest()
                .setPhone(phoneNumber)
                .setPassword(generateRandomPassword());
    }

    public SignInRequest setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public SignInRequest setPassword(String password) {
        this.password = password;
        return this;
    }

    private static String generateRandomPassword() {
        SecureRandom rand = new SecureRandom();
        return Integer.toString(rand.nextInt(10000));
    }
}

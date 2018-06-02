package se.tink.backend.main.mappers;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Objects;
import se.tink.backend.core.auth.AuthenticationStatus;
import se.tink.backend.core.auth.AuthenticationToken;
import se.tink.backend.core.auth.bankid.BankIdAuthentication;
import se.tink.backend.core.auth.bankid.BankIdAuthenticationStatus;
import se.tink.libraries.auth.AuthenticationMethod;

public class BankIdAuthenticationToAuthenticationTokenMapper {
    private static final Map<BankIdAuthenticationStatus, AuthenticationStatus> BANK_ID_AUTHENTICATION_TO_AUTHENTICATION_STATUS =
            ImmutableMap.<BankIdAuthenticationStatus, AuthenticationStatus>builder()
                    .put(BankIdAuthenticationStatus.AUTHENTICATED, AuthenticationStatus.AUTHENTICATED)
                    .put(BankIdAuthenticationStatus.AUTHENTICATION_ERROR, AuthenticationStatus.AUTHENTICATION_ERROR)
                    .put(BankIdAuthenticationStatus.AWAITING_BANKID_AUTHENTICATION,
                            AuthenticationStatus.AUTHENTICATION_ERROR)
                    .put(BankIdAuthenticationStatus.NO_USER, AuthenticationStatus.NO_USER)
                    .build();

    public static AuthenticationToken map(BankIdAuthentication bankIdAuthentication, String userId, String marketCode, String deviceId) {
        AuthenticationStatus status = BANK_ID_AUTHENTICATION_TO_AUTHENTICATION_STATUS
                .get(bankIdAuthentication.getStatus());

        AuthenticationToken.AuthenticationTokenBuilder authenticationToken = AuthenticationToken.builder()
                .withToken(bankIdAuthentication.getId())
                .withMethod(AuthenticationMethod.BANKID)
                .withStatus(status)
                .withNationalId(bankIdAuthentication.getNationalId())
                .withClientKey(bankIdAuthentication.getClientKey())
                .withOAuth2ClientId(bankIdAuthentication.getOAuth2ClientId())
                .withMarket(marketCode)
                .withAuthenticatedDeviceId(deviceId);

        if (Objects.equals(status, AuthenticationStatus.AUTHENTICATED)) {
            authenticationToken = authenticationToken.withUserId(userId);
        }

        return authenticationToken.build();
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class SwedbankDecoupledAuthenticatorTestData {

    static final AuthenticationResponse VALID_AUTHENTICATE_DECOUPLED_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "  \"authorizeId\": \"qwerty37-6f7c-455a-a7c4-9fc976qwerty\",\n"
                            + "  \"chosenScaMethod\": {\n"
                            + "    \"authenticationType\": \"Mobile BankID\",\n"
                            + "    \"authenticationMethodId\": \"MOBILE_ID\"\n"
                            + "  },\n"
                            + "  \"challengeData\": {\n"
                            + "    \"autoStartToken\": \"e7097dab-f702-4718-90c6-f91c2046ade8\"\n"
                            + "  },\n"
                            + "  \"psuMessage\": \"Loging via Tink_org\",\n"
                            + "  \"_links\": {\n"
                            + "    \"scaStatus\": {\n"
                            + "      \"href\": \"/psd2/v4/authorize-decoupled/authorize/qwerty37-6f7c-455a-a7c4-9fc976qwerty\"\n"
                            + "    }\n"
                            + "  }\n"
                            + "}",
                    AuthenticationResponse.class);

    static final AuthenticationResponse INVALID_AUTHENTICATE_DECOUPLED_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "  \"authorizeId\": \"qwerty37-6f7c-455a-a7c4-9fc976qwerty\",\n"
                            + "  \"chosenScaMethod\": {\n"
                            + "    \"authenticationType\": \"Mobile BankID\",\n"
                            + "    \"authenticationMethodId\": \"MOBILE_ID\"\n"
                            + "  },\n"
                            + "  \"challengeData\": {},\n"
                            + "  \"psuMessage\": \"Loging via Tink_org\",\n"
                            + "  \"_links\": {\n"
                            + "    \"scaStatus\": {\n"
                            + "      \"href\": \"/psd2/v4/authorize-decoupled/authorize/qwerty37-6f7c-455a-a7c4-9fc976qwerty\"\n"
                            + "    }\n"
                            + "  }\n"
                            + "}",
                    AuthenticationResponse.class);

    static final AuthenticationStatusResponse AUTH_FINALISED_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "  \"scaStatus\": \"finalised\",\n"
                            + "  \"authorizationCode\": \"mockedAuthorizationCode\"\n"
                            + "}",
                    AuthenticationStatusResponse.class);

    static final AuthenticationStatusResponse AUTH_STARTED_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n" + "\t\"scaStatus\": \"started\"\n" + "}",
                    AuthenticationStatusResponse.class);

    static final AuthenticationStatusResponse AUTH_FAILED_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n" + "  \"scaStatus\": \"failed\"\n" + "}",
                    AuthenticationStatusResponse.class);

    static final AuthenticationStatusResponse UNKNOWN_AUTH_STATUS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n" + "  \"scaStatus\": \"unknownStatus\"\n" + "}",
                    AuthenticationStatusResponse.class);

    static final AuthenticationStatusResponse BANK_ID_CANCEL_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "  \"tppMessages\": [\n"
                            + "    {\n"
                            + "      \"category\": \"ERROR\",\n"
                            + "      \"code\": \"USER_CANCEL\",\n"
                            + "      \"text\": \"Login process canceled by PSU\"\n"
                            + "    }\n"
                            + "  ]\n"
                            + "}",
                    AuthenticationStatusResponse.class);

    static final TokenResponse TOKEN_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "  \"access_token\":\"mockedAccessToken\",\n"
                            + "  \"token_type\":\"Bearer\",\n"
                            + "  \"expires_in\":3600,\n"
                            + "  \"refresh_token\":\"mockedRefreshToken\",\n"
                            + "  \"scope\":\"PSD2 PSD2account_balances PSD2account_transactions PSD2account_transactions_over90 PSD2account_list\"\n"
                            + "}",
                    TokenResponse.class);

    static final FetchAccountResponse SWEDBANK_ACCOUNTS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "  \"accounts\": [\n"
                            + "    {\n"
                            + "      \"resourceId\": \"mockedResourceId\",\n"
                            + "      \"iban\": \"SE5380000832791234567890\",\n"
                            + "      \"bban\": \"8327-9,123 456 789-0\",\n"
                            + "      \"currency\": \"SEK\",\n"
                            + "      \"product\": \"Privatkonto\",\n"
                            + "      \"cashAccountType\": \"CACC\",\n"
                            + "      \"name\": \"Vardag\",\n"
                            + "      \"bankId\": \"08999\"\n"
                            + "    }\n"
                            + "  ]\n"
                            + "}",
                    FetchAccountResponse.class);

    static final FetchAccountResponse SAVINGSBANK_ACCOUNTS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "  \"accounts\": [\n"
                            + "    {\n"
                            + "      \"resourceId\": \"mockedResourceId\",\n"
                            + "      \"iban\": \"SE5380000832791234567890\",\n"
                            + "      \"bban\": \"8284-9,123 456 789-0\",\n"
                            + "      \"currency\": \"SEK\",\n"
                            + "      \"product\": \"Privatkonto\",\n"
                            + "      \"cashAccountType\": \"CACC\",\n"
                            + "      \"name\": \"Vardag\",\n"
                            + "      \"bankId\": \"08284\"\n"
                            + "    }\n"
                            + "  ]\n"
                            + "}",
                    FetchAccountResponse.class);
}

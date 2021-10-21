package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.consent;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class SwedbankConsentHandlerTestData {

    static final ConsentResponse VALID_CONSENT_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "  \"consentId\": \"consentId123\",\n"
                            + "  \"consentStatus\": \"valid\"\n"
                            + "}",
                    ConsentResponse.class);

    static final ConsentResponse INVALID_CONSENT_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "  \"consentId\": \"consentId321\",\n"
                            + "  \"consentStatus\": \"rejected\"\n"
                            + "}",
                    ConsentResponse.class);

    static final FetchAccountResponse ACCOUNTS_RESPONSE =
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

    static final FetchAccountResponse EMPTY_ACCOUNTS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n" + "  \"accounts\": []\n" + "}", FetchAccountResponse.class);
}

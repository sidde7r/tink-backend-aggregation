package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen;

import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages.PSU_CREDENTIALS_INVALID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.i18n.LocalizableKey;

@RequiredArgsConstructor
@Slf4j
/**
 * This class, method, serves as an experiment on some sparkassen agents. ITE-2489 for more details.
 * We want to check if the different user message will help users to input correct password.
 */
public class SparkassenExperimentalLoginErrorHandling {

    private static final String LAB_RAT_PROVIDER = "de-sparkasse-hannover-ob";

    private static final LocalizableKey CUSTOM_TEMP_ERR_MESSAGE =
            new LocalizableKey(
                    "Incorrect login credentials. Please try entering the first 5 digits of your PIN or try again.");

    public static void handleIncorrectLogin(
            HttpResponseException httpResponseException, Provider provider) {
        if (provider.getName().equalsIgnoreCase(LAB_RAT_PROVIDER)
                && httpResponseException
                        .getResponse()
                        .getBody(String.class)
                        .contains(PSU_CREDENTIALS_INVALID)) {
            log.info("[ITE-2489] PSU_CREDENTIALS_INVALID encountered!");
            throw LoginError.INCORRECT_CREDENTIALS.exception(CUSTOM_TEMP_ERR_MESSAGE);
        }
    }
}

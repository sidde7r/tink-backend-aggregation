package se.tink.backend.aggregation.nxgen.controllers.utils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;

/**
 * This is the lowest level of interface an Agent or AuthController (a la Progressive auth) should
 * be using when triggering supplemental information flows. Note that supplemental information here
 * refers to all three kinds of flows (embedded fields, third-party apps/websites or Swedish
 * MobileBankID.
 */
public interface SupplementalInformationController {

    /**
     * Waits for the results of the request to the client synchrously on a given mfaId.
     *
     * @param mfaId The mfaId to wait for, the key is given back from the Async variants on this
     *     interface
     * @param waitFor the duration to wait until timing out the request.
     * @param unit the unit of the duration.
     * @return The results of the request, if any.
     */
    default Optional<Map<String, String>> waitForSupplementalInformation(
            String mfaId, long waitFor, TimeUnit unit) {
        return waitForSupplementalInformation(mfaId, waitFor, unit, false);
    }

    /**
     * VERY TEMPORARY WITH THE EXTRA BOOLEAN FLAG--- DO NOT USE
     *
     * @param mfaId
     * @param waitFor
     * @param unit
     * @param allowEmptyString
     * @return
     */
    Optional<Map<String, String>> waitForSupplementalInformation(
            String mfaId, long waitFor, TimeUnit unit, boolean allowEmptyString);

    /**
     * Starts an embedded dynamic authentication flow. This methods does the same as {@link
     * SupplementalInformationController#askSupplementalInformationAsync}, but also starts waiting
     * synchronously.
     *
     * @param fields the embedded Fields that the client should render.
     * @return The results of the request.
     * @throws SupplementalInfoException throws if no result is returned from the client.
     */
    Map<String, String> askSupplementalInformationSync(Field... fields)
            throws SupplementalInfoException;

    /**
     * Starts an embedded dynamic authentication flow. Requests for the client to render a new set
     * of embedded input Fields to answer by the end-user.
     *
     * <pre>
     * NB!
     * NB! Be very careful about using the async variant of embedded dynamic fields without knowing the consequences.
     * NB! Clients will be required to send in information to Tink Platform, meaning Agent cannot proceed on its own
     * NB! without waiting for the response. We've had incidents where subsequent SupplementalInformation requests
     * NB! got the response from the first one because Agent didn't wait for the first response.
     * NB!
     * </pre>
     *
     * @param fields
     * @return the mfaId that can be used to wait for the results.
     */
    String askSupplementalInformationAsync(Field... fields);

    /**
     * Starts a redirect/decoupled dynamic authentication flow. This methods does the same as {@link
     * SupplementalInformationController#openThirdPartyAppAsync}, but also starts waiting
     * synchronously.
     *
     * @param payload
     * @return the results from the request to the client, if any
     */
    Optional<Map<String, String>> openThirdPartyAppSync(ThirdPartyAppAuthenticationPayload payload);

    /**
     * Starts a redirect/decoupled dynamic authentication flow. Requests the client to open the
     * third-party app as specified in the payload.
     *
     * @param payload
     * @return the mfaId that can be used to wait for the results.
     */
    String openThirdPartyAppAsync(ThirdPartyAppAuthenticationPayload payload);

    /**
     * Starts a decoupled dynamic authentication flow with Mobile BankID. This methods does the same
     * as {@link SupplementalInformationController#openMobileBankIdSync}, but also starts waiting
     * synchronously.
     *
     * @param autoStartToken it is possible to supply null
     */
    void openMobileBankIdSync(String autoStartToken);

    /**
     * Starts a decoupled dynamic authentication flow with Mobile BankID. Requests the client to
     * open the decoupled Mobile BankId app. This should probably be deprecated and the generic
     * {@link SupplementalInformationController#openThirdPartyAppAsync} should be used instead.
     *
     * @param autoStartToken it is possible to supply null
     * @return the mfaId that can be used to wait for the results.
     */
    String openMobileBankIdAsync(String autoStartToken);
}

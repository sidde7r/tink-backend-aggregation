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
    Optional<Map<String, String>> waitForSupplementalInformation(
            String mfaId, long waitFor, TimeUnit unit);

    /**
     * Starts an embedded dynamic authentication flow. Requests for the client to render a new set
     * of embedded input Fields to answer by the end-user.
     *
     * @param fields the embedded Fields that the client should render.
     * @return The results of the request.
     * @throws SupplementalInfoException throws if no result is returned from the client.
     */
    Map<String, String> askSupplementalInformationSync(Field... fields)
            throws SupplementalInfoException;

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
}

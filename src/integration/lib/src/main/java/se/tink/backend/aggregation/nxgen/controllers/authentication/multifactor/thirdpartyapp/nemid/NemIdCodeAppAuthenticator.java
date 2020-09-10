package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.Errors;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.Status;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.error.NemIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.libraries.i18n.LocalizableKey;

public abstract class NemIdCodeAppAuthenticator<T> implements ThirdPartyAppAuthenticator<String> {

    protected final TinkHttpClient client;
    private String pollUrl;
    private NemIdCodeAppPollResponse pollResponse;

    public NemIdCodeAppAuthenticator(TinkHttpClient client) {
        this.client = client;
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        T initiationResponse = initiateAuthentication();

        // The poll endpoint might be slightly different for each NemID request, and requesting
        // on the wrong endpoint might cause delays or failures to obtain the user authorization
        this.pollUrl = getPollUrl(initiationResponse);

        return ThirdPartyAppResponseImpl.create(
                ThirdPartyAppStatus.WAITING, getInitialReference(initiationResponse));
    }

    protected abstract T initiateAuthentication();

    protected abstract String getPollUrl(T initiationResponse);

    protected abstract String getInitialReference(T initiationResponse);

    protected abstract void finalizeAuthentication();

    @Override
    public NemIdCodeAppResponse collect(String reference)
            throws AuthenticationException, AuthorizationException {
        this.pollResponse = pollCodeApp(reference);

        String pollStatus = pollResponse.getStatus();
        ThirdPartyAppStatus status;
        switch (pollStatus.toLowerCase()) {
            case Status.STATUS_OK:
                status =
                        pollResponse.isConfirmation()
                                ? ThirdPartyAppStatus.DONE
                                : ThirdPartyAppStatus.CANCELLED;
                finalizeAuthentication();
                break;
            case Status.STATUS_TIMEOUT:
                status = ThirdPartyAppStatus.WAITING;
                break;
            case Status.EXPIRED:
                status = ThirdPartyAppStatus.TIMED_OUT;
                break;
            case Status.OVERWRITTEN:
                throw NemIdError.INTERRUPTED.exception();
            default:
                throw new IllegalStateException(
                        String.format("Unknown code app poll response: %s.", pollStatus));
        }

        return new NemIdCodeAppResponse(status, reference, pollResponse);
    }

    protected NemIdCodeAppPollResponse pollCodeApp(String ticket) {
        NemIdCodeAppPollRequest request = new NemIdCodeAppPollRequest(ticket);

        try {
            return client.request(pollUrl)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(NemIdCodeAppPollResponse.class, request);
        } catch (HttpClientException e) {
            if (Errors.READ_TIMEOUT_ERROR.equals(e.getCause().getMessage())) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(
                        "Error message: " + Errors.READ_TIMEOUT_ERROR);
            }
            throw e;
        }
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        /*
         * NemID app switching documentation:
         * https://www.nets.eu/dk-da/kundeservice/nemid-tjenesteudbyder/NemID-tjenesteudbyderpakken/Documents/NMAS%20app%20switch.pdf
         */

        ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();

        ThirdPartyAppAuthenticationPayload.Android androidPayload =
                new ThirdPartyAppAuthenticationPayload.Android();
        androidPayload.setIntent("android-app://dk.e_nettet.mobilekey.everyone");
        androidPayload.setPackageName("dk.e_nettet.mobilekey.everyone");
        payload.setAndroid(androidPayload);

        ThirdPartyAppAuthenticationPayload.Ios iOsPayload =
                new ThirdPartyAppAuthenticationPayload.Ios();
        iOsPayload.setAppStoreUrl("https://apps.apple.com/dk/app/nemid-codeapp/id1300533299");
        // TODO this is the app scheme, but it shouldn't be used to open the app, according to the
        // docs
        iOsPayload.setAppScheme("nemid-codeapp");
        // TODO check real Tink app scheme
        // TODO on a later moment, return URL needs to be configurable and customized per client
        iOsPayload.setDeepLinkUrl("https://codeapp.e-nettet.dk?return=tink://");
        payload.setIos(iOsPayload);

        return payload;
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }

    public Optional<NemIdCodeAppPollResponse> getPollResponse() {
        if (pollResponse == null) {
            return Optional.empty();
        }
        return Optional.of(pollResponse);
    }
}

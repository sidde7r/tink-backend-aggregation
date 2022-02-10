package se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.client.ConsorsbankAuthApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@RequiredArgsConstructor
public class ConsorsbankAuthenticator
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private static final String STATE = "state";
    private static final String NOT_OK = "nok";

    private final ConsorsbankAuthApiClient apiClient;
    private final ConsorsbankStorage storage;
    private final SupplementalInformationController supplementalInformationController;
    private final StrongAuthenticationState strongAuthenticationState;
    private final Credentials credentials;
    private final LocalDateTimeSource localDateTimeSource;

    private final String redirectUrl;

    @Override
    public void autoAuthenticate() {
        String consentId = storage.getConsentId();
        if (consentId == null || !apiClient.fetchConsentDetails(consentId).isValid()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        ConsentRequest consentRequest = buildConsentRequest();
        URL redirectUrlWithState =
                new URL(redirectUrl).queryParam(STATE, strongAuthenticationState.getState());
        URL redirectUrlNotOk = redirectUrlWithState.queryParam(NOT_OK, String.valueOf(true));

        ConsentResponse consent =
                apiClient.createConsent(consentRequest, redirectUrlWithState, redirectUrlNotOk);
        storage.saveConsentId(consent.getConsentId());

        return ThirdPartyAppAuthenticationPayload.of(new URL(consent.getLinks().getScaRedirect()));
    }

    private ConsentRequest buildConsentRequest() {
        AccessEntity accessEntity =
                AccessEntity.builder()
                        .accounts(Collections.emptyList())
                        .balances(Collections.emptyList())
                        .transactions(Collections.emptyList())
                        .build();
        return ConsentRequest.buildTypicalRecurring(accessEntity, localDateTimeSource);
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference) {
        Optional<Map<String, String>> supplementalInfo =
                supplementalInformationController.waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(),
                        ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                        TimeUnit.MINUTES);

        ThirdPartyAppStatus result;
        if (!supplementalInfo.isPresent()) {
            result = ThirdPartyAppStatus.TIMED_OUT;
        } else {
            if (supplementalInfo.get().containsKey(NOT_OK)) {
                result = ThirdPartyAppStatus.CANCELLED;
            } else {
                result = checkResultingConsent();
            }
        }
        return ThirdPartyAppResponseImpl.create(result);
    }

    private ThirdPartyAppStatus checkResultingConsent() {
        ConsentDetailsResponse consentDetails =
                apiClient.fetchConsentDetails(storage.getConsentId());
        if (consentDetails.isValid()) {
            storage.saveConsentAccess(consentDetails.getAccess());
            credentials.setSessionExpiryDate(consentDetails.getValidUntil());
            return ThirdPartyAppStatus.DONE;
        } else {
            return ThirdPartyAppStatus.AUTHENTICATION_ERROR;
        }
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }
}

package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.QueryKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class DnbPaymentController extends PaymentController {
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final PersistentStorage persistentStorage;

    public DnbPaymentController(
            DnbPaymentExecutor paymentExecutor,
            SupplementalInformationHelper supplementalInformationHelper,
            PersistentStorage persistentStorage,
            StrongAuthenticationState strongAuthenticationState) {
        super(paymentExecutor, paymentExecutor);

        this.supplementalInformationHelper = supplementalInformationHelper;
        this.persistentStorage = persistentStorage;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    private void init() {
        ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    private ThirdPartyAppResponse<String> collect() {
        this.supplementalInformationHelper.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(),
                ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                TimeUnit.MINUTES);

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    private void openThirdPartyApp(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload payload =
                getAppPayload(authorizeUrl)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Can't translate authorizeUrl to App Payload "
                                                        + authorizeUrl));
        this.supplementalInformationHelper.openThirdPartyApp(payload);
    }

    private Optional<ThirdPartyAppAuthenticationPayload> getAppPayload(URL authorizeUrl) {
        return Optional.ofNullable(ThirdPartyAppAuthenticationPayload.of(authorizeUrl));
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {

        init();

        String id = paymentMultiStepRequest.getPayment().getUniqueId();
        URL authorizeUrl =
                new URL(
                        persistentStorage.get(id)
                                + "&"
                                + Form.builder()
                                        .put(QueryKeys.STATE, strongAuthenticationState.getState())
                                        .build());
        openThirdPartyApp(authorizeUrl);
        collect();

        return super.sign(paymentMultiStepRequest);
    }
}

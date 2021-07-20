package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment;

import com.google.common.base.Strings;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.NoCodeParamException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class OpBankPaymentController extends PaymentController {

    private static final long WAIT_FOR_MINUTES = 9L;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final PersistentStorage persistentStorage;

    public OpBankPaymentController(
            OpBankPaymentExecutor paymentExecutor,
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState,
            PersistentStorage persistentStorage) {
        super(paymentExecutor);

        this.supplementalInformationHelper = supplementalInformationHelper;
        this.strongAuthenticationState = strongAuthenticationState;
        this.persistentStorage = persistentStorage;
    }

    private void openThirdPartyApp(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload payload =
                ThirdPartyAppAuthenticationPayload.of(authorizeUrl);

        this.supplementalInformationHelper.openThirdPartyApp(payload);
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        persistentStorage.put("State", strongAuthenticationState.getState());
        PaymentResponse paymentResponse = super.create(paymentRequest);

        URL authorizeUrl = URL.of(persistentStorage.get("URL"));

        openThirdPartyApp(authorizeUrl);
        Map<String, String> callbackData =
                this.supplementalInformationHelper
                        .waitForSupplementalInformation(
                                strongAuthenticationState.getSupplementalKey(),
                                WAIT_FOR_MINUTES,
                                TimeUnit.MINUTES)
                        .orElseThrow(ThirdPartyAppError.TIMED_OUT::exception);

        String code = callbackData.getOrDefault(OAuth2Constants.CallbackParams.CODE, null);
        if (Strings.isNullOrEmpty(code)) {
            throw new NoCodeParamException(
                    "callbackData did not contain 'code' and no error was handled");
        }
        persistentStorage.put("Code", code);

        return paymentResponse;
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {

        return super.sign(paymentMultiStepRequest);
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.CallbackFields.DESCRIPTION;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.CallbackFields.ERROR;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.CallbackFields.STATE;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import se.tink.backend.aggregation.agents.exceptions.payment.DebtorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;

@Slf4j
public class CmcicCallbackErrorHandler implements CmcicCallbackHandlingStrategy {

    public static final String CANCELED_BY_THE_CLIENT = "operation canceled by the client";
    public static final String CANCELED_BY_USER = "Canceled by user";
    public static final String THE_PSU_CANCELLED = "The PSU cancelled the operation";
    public static final String SAME_CREDITOR_DEBITOR =
            "Compte à créditer identique au compte à débiter";
    public static final String EUROPEAN_BENIFICIARY_REJECTED =
            "Vous n'êtes pas autorisé à saisir un bénéficiaire européen";
    public static final String INCORRECT_SYMBOL =
            "L'intitulé bénéf. ne peut contenir uniquement le symbole '";
    public static final String INVALID_REQUEST = "invalid_request";
    public static final String ACCESS_DENIED = "access_denied";
    private final Map<ImmutablePair<String, String>, Supplier<PaymentException>>
            errorHandlingMapping;

    public static CmcicCallbackErrorHandler create() {
        Map<ImmutablePair<String, String>, Supplier<PaymentException>> errorHandlingMapping =
                new HashMap<>();
        mapPaymentCancelled(errorHandlingMapping, CANCELED_BY_THE_CLIENT);
        mapPaymentCancelled(errorHandlingMapping, CANCELED_BY_USER);
        mapPaymentCancelled(errorHandlingMapping, THE_PSU_CANCELLED);
        mapInvalidRequest(
                errorHandlingMapping,
                SAME_CREDITOR_DEBITOR,
                DebtorValidationException::canNotFromSameUser);
        mapInvalidRequest(
                errorHandlingMapping, EUROPEAN_BENIFICIARY_REJECTED, PaymentRejectedException::new);
        mapInvalidRequest(
                errorHandlingMapping,
                INCORRECT_SYMBOL,
                () -> new PaymentValidationException(PaymentValidationException.DEFAULT_MESSAGE));

        return new CmcicCallbackErrorHandler(errorHandlingMapping);
    }

    private static void mapInvalidRequest(
            Map<ImmutablePair<String, String>, Supplier<PaymentException>> errorHandlingMapping,
            String description,
            Supplier<PaymentException> exceptionSupplier) {
        errorHandlingMapping.put(getInvalidRequestKey(description), exceptionSupplier);
    }

    private static void mapPaymentCancelled(
            Map<ImmutablePair<String, String>, Supplier<PaymentException>> errorHandlingMapping,
            String s) {
        errorHandlingMapping.put(getAccessDeniedKey(s), PaymentCancelledException::new);
    }

    private static ImmutablePair<String, String> getInvalidRequestKey(String description) {
        return ImmutablePair.of(INVALID_REQUEST, description);
    }

    private static ImmutablePair<String, String> getAccessDeniedKey(String description) {
        return ImmutablePair.of(ACCESS_DENIED, description);
    }

    public CmcicCallbackErrorHandler(
            Map<ImmutablePair<String, String>, Supplier<PaymentException>> errorHandlingMapping) {
        this.errorHandlingMapping = errorHandlingMapping;
    }

    @Override
    public void handleCallback(CmcicCallbackData cmcicCallbackData) {
        Map<String, String> expectedCallbackData = cmcicCallbackData.getExpectedCallbackData();
        String error = expectedCallbackData.get(ERROR);
        String error_description = expectedCallbackData.get(DESCRIPTION);
        String state = expectedCallbackData.get(STATE);
        log.info(
                "Received error callback. State: {}, error: {}, error description: {}",
                state,
                error,
                error_description);
        ImmutablePair<String, String> errorPair = ImmutablePair.of(error, error_description);
        throw errorHandlingMapping
                .getOrDefault(errorPair, this::getExceptionForUnmappedError)
                .get();
    }

    private PaymentRejectedException getExceptionForUnmappedError() {
        log.warn("Unmapped error from callback detected!");
        return new PaymentRejectedException();
    }
}

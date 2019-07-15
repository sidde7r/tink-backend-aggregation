package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities;

import java.util.function.BiFunction;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;

@JsonObject
public class TppMessagesEntity {

    private static final GenericTypeMapper<BiFunction<String, Throwable, PaymentException>, String>
            errorCodeToPaymentExceptionMapper =
                    GenericTypeMapper
                            .<BiFunction<String, Throwable, PaymentException>, String>
                                    genericBuilder()
                            .put(
                                    PaymentValidationException::new,
                                    "FORMAT_ERROR",
                                    "PARAMETER_NOT_CONSISTENT",
                                    "PARAMETER_NOT_SUPPORTED",
                                    "SERVICE_INVALID",
                                    "RESOURCE_UNKNOWN",
                                    "RESOURCE_EXPIRED",
                                    "TIMESTAMP_INVALID",
                                    "PERIOD_INVALID",
                                    "SCA_METHOD_UNKNOWN",
                                    "CONSENT_UNKNOWN",
                                    "PAYMENT_FAILED",
                                    "EXECUTION_DATE_INVALID")
                            .put(
                                    PaymentAuthenticationException::new,
                                    "CERTIFICATE_INVALID",
                                    "CERTIFICATE_EXPIRED",
                                    "CERTIFICATE_REVOKE",
                                    "CERTIFICATE_MISSING",
                                    "SIGNATURE_INVALID",
                                    "SIGNATURE_MISSING",
                                    "CORPORATE_ID_INVALID",
                                    "PSU_CREDENTIALS_INVALID",
                                    "CONSENT_INVALID",
                                    "CONSENT_EXPIRED",
                                    "TOKEN_UNKNOWN",
                                    "TOKEN_INVALID",
                                    "TOKEN_EXPIRED",
                                    "REQUIRED_KID_MISSING")
                            .put(
                                    PaymentAuthorizationException::new,
                                    "CERTIFICATE_BLOCKED",
                                    "RESOURCE_BLOCKED")
                            .build();
    private String category;
    private String code;
    private String path;
    private String text;

    public PaymentException buildRelevantException(Throwable cause) {

        return errorCodeToPaymentExceptionMapper
                .translate(code)
                .map(e -> e.apply(code, cause))
                .orElseGet(() -> new PaymentException(this.toString(), cause));
    }
}

package se.tink.backend.aggregation.nxgen.http.log.constants;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

public final class HttpLoggingConstants {
    // In lower case
    public static final Set<String> NON_SENSITIVE_HEADER_FIELDS =
            ImmutableSet.of(
                    "accept",
                    "accept-charset",
                    "accept-datetime",
                    "accept-encoding",
                    "accept-language",
                    "accept-ranges",
                    "access-control-allow-origin",
                    "age",
                    "allow",
                    "cache-control",
                    "connection",
                    "content-encoding",
                    "content-language",
                    "content-length",
                    "content-type",
                    "correlation-id",
                    "date",
                    "encoded-error-message",
                    "error-code",
                    "error-message",
                    "expires",
                    "forwarded",
                    "host",
                    "if-modified-since",
                    "if-unmodified-since",
                    "jws-signature",
                    "language",
                    "last-modified",
                    "pragma",
                    "proxy-connection",
                    "psu-corporate-id",
                    "psu-id-type",
                    "psu-ip-address",
                    "psu-ip-port",
                    "psu-session",
                    "psu-user-agent",
                    "ratelimit-limit",
                    "ratelimit-remaining",
                    "ratelimit-reset",
                    "referer",
                    "request-id",
                    "retry-after",
                    "send-date",
                    "server",
                    "signature",
                    "status",
                    "strict-transport-security",
                    "tpp-id",
                    "tpp-nok-redirect-uri",
                    "tpp-redirect-uri",
                    "tpp-request-id",
                    "tpp-signature-certificate",
                    "tpp-x-request-id",
                    "transfer-encoding",
                    "user-agent",
                    "vary",
                    "via",
                    "x-aggregator",
                    "x-amex-request-id",
                    "x-amz-apigw-id",
                    "x-amzn-requestid",
                    "x-amzn-trace-id",
                    "x-content-type-options",
                    "x-correlation-id",
                    "x-correlationid",
                    "x-fapi-financial-id",
                    "x-fapi-interaction-id",
                    "x-forwarded-for",
                    "x-forwarded-host",
                    "x-frame-options",
                    "x-global-transaction-id",
                    "x-ing-reqid",
                    "x-ing-response-id",
                    "x-jws-signature",
                    "x-luminati-error",
                    "x-powered-by",
                    "x-rate-limit-limit",
                    "x-rate-limit-remaining",
                    "x-rate-limit-reset",
                    "x-ratelimit-limit-minute",
                    "x-ratelimit-limit-second",
                    "x-ratelimit-limit",
                    "x-ratelimit-remaining-minute",
                    "x-ratelimit-remaining-second",
                    "x-ratelimit-remaining",
                    "x-request-id",
                    "x-response-id",
                    "x-servicerequestid",
                    "x-xss-protection");
}

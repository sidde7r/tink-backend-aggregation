package se.tink.backend.aggregation.nxgen.http.log;

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
                    "date",
                    "expires",
                    "forwarded",
                    "if-modified-since",
                    "if-unmodified-since",
                    "host",
                    "language",
                    "last-modified",
                    "pragma",
                    "proxy-connection",
                    "psu-id-type",
                    "psu-ip-address",
                    "referer",
                    "server",
                    "status",
                    "tpp-redirect-uri",
                    "tpp-request-id",
                    "transfer-encoding",
                    "user-agent",
                    "vary",
                    "via",
                    "x-fapi-interaction-id",
                    "x-forwarded-for",
                    "x-forwarded-host",
                    "x-global-transaction-id",
                    "x-ing-reqid",
                    "x-ing-response-id",
                    "x-powered-by",
                    "x-request-id",
                    "x-response-id");
}

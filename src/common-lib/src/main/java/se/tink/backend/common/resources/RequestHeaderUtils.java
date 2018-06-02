package se.tink.backend.common.resources;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.backend.core.ClientType;
import se.tink.api.headers.TinkHttpHeaders;

public class RequestHeaderUtils {

    public static String getRequestHeader(MultivaluedMap<String, String> headers, String key) {
        if (headers == null) {
            return null;
        }

        List<String> header = headers.get(key);

        if (header == null || header.isEmpty()) {
            return null;
        }

        return Iterables.getFirst(header, null);
    }

    // TODO: Make this return an Optional.
    public static String getRequestHeader(HttpHeaders headers, String key) {
        if (headers == null) {
            return null;
        }

        List<String> header = headers.getRequestHeader(key);

        if (header == null || header.isEmpty()) {
            return null;
        }

        return Iterables.getFirst(header, null);
    }

    public static String getUserAgent(HttpHeaders headers) {
        return getRequestHeader(headers, HttpHeaders.USER_AGENT);
    }

    public static Optional<String> getRemoteIp(HttpHeaders headers) {

        // IllegalStateException will be thrown if the request is done for InProcess calls
        try {
            String remoteIp = getRequestHeader(headers, TinkHttpHeaders.FORWARDED_FOR_HEADER_NAME);
            if (Strings.isNullOrEmpty(remoteIp)) {
                return Optional.empty();
            } else {
                return Optional.of(remoteIp);
            }
        } catch (IllegalStateException e) {
            return Optional.empty();
        }

    }

    public static boolean isAndroidRequest(HttpHeaders headers) {
        String userAgent = getUserAgent(headers);

        if (userAgent == null) {
            return false;
        }

        return userAgent.contains("Android");
    }

    public static boolean isIosRequest(HttpHeaders headers) {
        String userAgent = getUserAgent(headers);

        if (userAgent == null) {
            return false;
        }

        return userAgent.contains("iOS");
    }

    public static boolean isMobileRequest(HttpHeaders headers) {
        return isIosRequest(headers) || isAndroidRequest(headers);
    }

    public static boolean isBackgroundRequest(HttpHeaders headers) {
        String backgroundRequestHeaderValue = getRequestHeader(headers, TinkHttpHeaders.BACKGROUND_REQUEST_HEADER_NAME);

        if (Strings.isNullOrEmpty(backgroundRequestHeaderValue)) {
            return false;
        }

        try {
            return Boolean.parseBoolean(backgroundRequestHeaderValue);
        } catch (Exception e) {
            return false;
        }
    }

    public static Date getClientClock(HttpHeaders headers) {
        if (headers == null) {
            return null;
        }

        String clientClock = getRequestHeader(headers, TinkHttpHeaders.CLIENT_CLOCK_HEADER_NAME);

        if (clientClock == null) {
            return null;
        }

        return se.tink.libraries.date.DateUtils.parseDate(clientClock);
    }

    public static ClientType getClientType(HttpHeaders headers) {
        if (RequestHeaderUtils.isIosRequest(headers)) {
            return ClientType.IOS;
        } else if (RequestHeaderUtils.isAndroidRequest(headers)) {
            return ClientType.ANDROID;
        } else {
            return ClientType.OTHER;
        }
    }

    public static Map<String, String> getHeadersMap(HttpHeaders headers) {
        if (headers != null) {
            return headers.getRequestHeaders().entrySet().stream()
                    .filter(entry -> !entry.getValue().isEmpty())
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0)));
        } else {
            return Collections.emptyMap();
        }
    }
}

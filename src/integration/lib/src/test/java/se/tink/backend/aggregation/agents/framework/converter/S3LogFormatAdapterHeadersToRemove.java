package se.tink.backend.aggregation.agents.framework.converter;

import java.util.Arrays;

public enum S3LogFormatAdapterHeadersToRemove {
    X_CONTROL_HEADERS("-Control"),
    HEADERS_STARTS_BY_XU("X-U"),
    HEADERS_STARTS_BY_XX("X-X"),
    HEADERS_STARTS_BY_XA("X-A"),
    HEADERS_STARTS_BY_X_AXP_LOWERCASE("x-a"),
    HEADERS_STARTS_BY_CF("CF-"),
    HEADERS_STARTS_BY_CF_LOWERCASE("cf-"),
    HEADER_PRAGMA("Pragma"),
    HEADER_NEL("NEL"),
    HEADERS_SERVER("Server"),
    HEADER_OPENTRACING("Opentracing");

    private String header;

    S3LogFormatAdapterHeadersToRemove(String header) {
        this.header = header;
    }

    public String getName() {
        return header;
    }

    public static String[] asArray() {
        return Arrays.stream(S3LogFormatAdapterHeadersToRemove.values())
                .map(S3LogFormatAdapterHeadersToRemove::getName)
                .toArray(String[]::new);
    }
}

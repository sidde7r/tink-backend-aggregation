package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.filter;

import lombok.Value;

@Value
public class ICSRateLimitFilterProperties {

    int retrySleepMillisecondsMin;
    int retrySleepMillisecondsMax;
    int numberOfRetries;
}

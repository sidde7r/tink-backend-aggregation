package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import lombok.Value;

@Value
public class SibsRateLimitFilterProperties {

    long retrySleepMillisecondsMin;
    long retrySleepMillisecondsMax;
    int numberOfRetries;
}

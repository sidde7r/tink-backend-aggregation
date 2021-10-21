package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import lombok.Value;

@Value
public class SibsRetryFilterProperties {
    int maxNumRetries;
    int retrySleepMilliseconds;
}

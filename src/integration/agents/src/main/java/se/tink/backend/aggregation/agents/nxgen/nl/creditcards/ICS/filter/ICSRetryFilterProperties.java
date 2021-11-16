package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.filter;

import lombok.Value;

@Value
public class ICSRetryFilterProperties {
    int maxNumRetries;
    int retrySleepMilliseconds;
}

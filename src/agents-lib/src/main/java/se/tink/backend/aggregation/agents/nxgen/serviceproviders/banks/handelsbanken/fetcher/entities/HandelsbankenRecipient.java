package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.deserializers.StringCleaningDeserializer;

@JsonObject
public class HandelsbankenRecipient {
    @JsonDeserialize(using = StringCleaningDeserializer.class)
    private String name;
    private String reference;
    @JsonDeserialize(using = StringCleaningDeserializer.class)
    private String additionalInfo;

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public String getName() {
        return name;
    }
}

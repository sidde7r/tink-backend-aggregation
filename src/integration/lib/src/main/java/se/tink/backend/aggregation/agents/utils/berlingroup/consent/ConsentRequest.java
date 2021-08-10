package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ConsentRequest {
    private AccessEntity access;
    private boolean recurringIndicator;
    private String validUntil;
    private int frequencyPerDay;
    private boolean combinedServiceIndicator;

    public static ConsentRequest buildTypicalRecurring(AccessEntity access, String validUntil) {
        return new ConsentRequest(access, true, validUntil, 4, false);
    }
}

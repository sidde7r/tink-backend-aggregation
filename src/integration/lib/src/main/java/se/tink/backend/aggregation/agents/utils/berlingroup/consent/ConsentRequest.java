package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ConsentRequest {
    private AccessEntity access;
    private boolean recurringIndicator;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validUntil;

    private int frequencyPerDay;
    private boolean combinedServiceIndicator;

    public static ConsentRequest buildTypicalRecurring(
            AccessEntity access, LocalDateTimeSource localDateTimeSource) {
        return new ConsentRequest(
                access, true, localDateTimeSource.now().plusDays(89).toLocalDate(), 4, false);
    }
}

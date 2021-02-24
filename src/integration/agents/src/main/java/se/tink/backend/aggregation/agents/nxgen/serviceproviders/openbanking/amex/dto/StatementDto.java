package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class StatementDto {

    private LocalDate startDate;
    private LocalDate endDate;
    private int index;

    public String getEndDateAsString() {
        return endDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}

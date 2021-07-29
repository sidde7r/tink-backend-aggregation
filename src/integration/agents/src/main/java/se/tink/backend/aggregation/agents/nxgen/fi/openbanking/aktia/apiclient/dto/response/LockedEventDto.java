package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class LockedEventDto {

    private BigDecimal amount;

    private String description;

    private String eventName;

    private String id;

    private LocalDate fromDate;

    private LocalDate toDate;
}

package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;

@JsonObject
@Data
public class LockedEventDto {

    private BigDecimal amount;

    private String description;

    private String eventName;

    private String id;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate fromDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate toDate;
}

package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import se.tink.agent.sdk.utils.serialization.local_date.LocalDateTimeDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class NickelAccountOverview {

    BigDecimal balance;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    LocalDateTime creationDate;
}

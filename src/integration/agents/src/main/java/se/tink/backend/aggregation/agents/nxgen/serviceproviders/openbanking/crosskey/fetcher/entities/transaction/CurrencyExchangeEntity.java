package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.transaction;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateTimeDeserializer;

@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class CurrencyExchangeEntity {

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime quotationDate;

    private String contractIdentification;
    private Integer exchangeRate;
    private AmountEntity instructedAmount;
    private String sourceCurrency;
    private String targetCurrency;
    private String unitCurrency;
}

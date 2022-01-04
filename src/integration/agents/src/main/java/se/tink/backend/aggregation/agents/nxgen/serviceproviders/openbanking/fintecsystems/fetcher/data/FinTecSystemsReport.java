package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.fetcher.data;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class FinTecSystemsReport {

    private ReportBaseData baseData;
}

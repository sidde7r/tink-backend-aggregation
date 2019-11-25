package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.creditcard;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.generic.SectionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetSummaryBodyEntity {
    @JsonProperty("ResponsabilidadesHoje")
    private BigDecimal responsibilitiesToday;

    @JsonProperty("RecursosHoje")
    private BigDecimal todayFeatures;

    @JsonProperty("NomeCliente")
    private String customerName;

    @JsonProperty("DataConsulta")
    private String dataQuery;

    @JsonProperty("Responsabilidades")
    private List<SectionEntity> responsibilities;

    public BigDecimal getResponsibilitiesToday() {
        return responsibilitiesToday;
    }

    public BigDecimal getTodayFeatures() {
        return todayFeatures;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getDataQuery() {
        return dataQuery;
    }

    public List<SectionEntity> getResponsibilities() {
        return responsibilities;
    }
}

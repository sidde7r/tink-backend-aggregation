package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class InfoURLsEntity {
    private String annualReport;
    private String factSheet;
    private String fundCompanyHomepage;
    private String informationBrochure;
    private String fundConditions;
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InfoURLsEntity {
    private String annualReport;
    private String factSheet;
    private String fundCompanyHomepage;
    private String informationBrochure;
    private String fundConditions;

    public String getAnnualReport() {
        return annualReport;
    }

    public String getFactSheet() {
        return factSheet;
    }

    public String getFundCompanyHomepage() {
        return fundCompanyHomepage;
    }

    public String getInformationBrochure() {
        return informationBrochure;
    }

    public String getFundConditions() {
        return fundConditions;
    }
}

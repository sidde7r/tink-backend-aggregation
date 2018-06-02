package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentsSummary {
    private String summaryResultCode;
    private String summaryResult;
    private InvestmentSummaryEntity funds;
    private InvestmentSummaryEntity stocks;
    private InvestmentSummaryEntity insuranceSavings;

    public String getSummaryResultCode() {
        return summaryResultCode;
    }

    public String getSummaryResult() {
        return summaryResult;
    }

    public InvestmentSummaryEntity getFunds() {
        return funds;
    }

    public InvestmentSummaryEntity getStocks() {
        return stocks;
    }

    public InvestmentSummaryEntity getInsuranceSavings() {
        return insuranceSavings;
    }
}

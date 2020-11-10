package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.investment.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class InstrumentEntity {

    private static final String FUND_TYPE = "FUND";
    private static final String CASH_TYPE = "CASH";
    private static final String EQUITY_TYPE = "EQUITY";
    private static final String DERIVATIVE_TYPE = "DERIVATIVE";

    private String id;
    private String currency;
    private String isin;
    private String issuer;
    private String instrumentType;

    private double price;

    public boolean isFund() {
        return FUND_TYPE.equals(instrumentType);
    }

    public boolean isCash() {
        return CASH_TYPE.equals(instrumentType);
    }

    public boolean isDerivative() {
        return DERIVATIVE_TYPE.equals(instrumentType);
    }

    public boolean isEquity() {
        return EQUITY_TYPE.equals(instrumentType);
    }
}

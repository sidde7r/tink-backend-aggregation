package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class InstrumentEntity {

    @JsonIgnore private static final String FUND_TYPE = "FUND";
    @JsonIgnore private static final String CASH_TYPE = "CASH";
    @JsonIgnore private static final String EQUITY_TYPE = "EQUITY";
    @JsonIgnore private static final String DERIVATIVE_TYPE = "DERIVATIVE";

    private String currency;
    private String id;
    private String isin;
    private String issuer;
    private double price;
    private String instrumentType;

    public String getCurrency() {
        return currency;
    }

    public String getId() {
        return id;
    }

    public String getIsin() {
        return isin;
    }

    public String getIssuer() {
        return issuer;
    }

    public double getPrice() {
        return price;
    }

    public String getRawType() {
        return instrumentType;
    }

    @JsonIgnore
    public boolean isFund() {
        return FUND_TYPE.equals(instrumentType);
    }

    @JsonIgnore
    public boolean isCash() {
        return CASH_TYPE.equals(instrumentType);
    }

    @JsonIgnore
    public boolean isDerivative() {
        return DERIVATIVE_TYPE.equals(instrumentType);
    }

    @JsonIgnore
    public boolean isEquity() {
        return EQUITY_TYPE.equals(instrumentType);
    }
}

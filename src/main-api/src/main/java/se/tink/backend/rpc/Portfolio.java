package se.tink.backend.rpc;

import io.swagger.annotations.ApiModelProperty;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Portfolio {

    @ApiModelProperty(value="The internal identifier of the user which owns the portfolio.", example = "a52e9890520d4ec38cc0d4526a4cdcbe")
    private UUID userId;

    @ApiModelProperty(value="The internal identifier of the account which has the portfolio.", example = "1d764c9f9141434aa23485c03561428d")
    private UUID accountId;

    @ApiModelProperty(value="The internal identifier of the portfolio.", example = "4c72494cc67f472f9f0ec2072600fe93")
    private UUID id;

    @ApiModelProperty(value="The total current value of the entire portfolio and all its underlying instruments.", example = "231924.16")
    private Double totalValue;

    @ApiModelProperty(value="The total profit of the entire portfolio. This includes both historical (real) profit, and current (potential) profit.", example = "48673.11")
    private Double totalProfit;

    @ApiModelProperty(value="The funds, on this portfolio, available for purchasing instruments, or to be transferred away.", example = "123.5")
    private Double cashValue;

    @ApiModelProperty(value="The type of the portfolio.", example = "DEPOT", allowableValues = se.tink.backend.core.Portfolio.Type.DOCUMENTED)
    private se.tink.backend.core.Portfolio.Type type;

    @ApiModelProperty(value="The instruments which this portfolio holds.")
    private List<Instrument> instruments;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(Double totalValue) {
        this.totalValue = totalValue;
    }

    public Double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(Double totalProfit) {
        this.totalProfit = totalProfit;
    }

    public se.tink.backend.core.Portfolio.Type getType() {
        return type;
    }

    public void setType(se.tink.backend.core.Portfolio.Type type) {
        this.type = type;
    }

    public List<Instrument> getInstruments() {
        if (instruments == null) {
            return Collections.emptyList();
        }

        return instruments;
    }

    public void setInstruments(List<Instrument> instruments) {
        this.instruments = instruments;
    }

    public Double getCashValue() {
        return cashValue;
    }

    public void setCashValue(Double cashValue) {
        this.cashValue = cashValue;
    }
}

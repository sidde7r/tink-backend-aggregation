package se.tink.backend.rpc;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;

public class InvestmentResponse {

    @ApiModelProperty(value = "A list of the user's portfolios.")
    private final List<Portfolio> portfolios;

    public InvestmentResponse(List<Portfolio> portfolios) {
        this.portfolios = portfolios;
    }

    public List<Portfolio> getPortfolios() {
        return portfolios;
    }
}

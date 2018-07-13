package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankTradingAssetPortfolio;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TradingAssetsPortfoliosResponse extends ArrayList<OpBankTradingAssetPortfolio> {
    @JsonIgnore
    public List<OpBankTradingAssetPortfolio> getPortfolios() {
        return stream()
                .filter(OpBankTradingAssetPortfolio::isValid)
                .collect(Collectors.toList());
    }
}



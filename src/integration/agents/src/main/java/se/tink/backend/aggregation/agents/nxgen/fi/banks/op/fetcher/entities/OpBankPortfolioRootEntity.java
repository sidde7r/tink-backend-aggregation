package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpBankPortfolioRootEntity {
    private int status;
    private List<OpBankPortfoliosEntity> portfolios;

    public List<OpBankPortfoliosEntity> getPortfolios() {
        if (portfolios != null) {
            return portfolios.stream()
                    .filter(OpBankPortfoliosEntity::isValid)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }
}

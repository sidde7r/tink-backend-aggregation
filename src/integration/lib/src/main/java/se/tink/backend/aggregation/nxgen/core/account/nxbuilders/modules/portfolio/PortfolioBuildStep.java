package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio;

public interface PortfolioBuildStep {

    /** @param rawType Portfolio type as received from the bank */
    PortfolioBuildStep setRawType(String rawType);

    PortfolioModule build();
}

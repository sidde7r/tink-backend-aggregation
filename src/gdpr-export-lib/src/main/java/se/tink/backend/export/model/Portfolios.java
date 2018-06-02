package se.tink.backend.export.model;

import java.util.List;
import se.tink.backend.export.model.submodels.ExportPortfolio;

public class Portfolios {

    private final List<ExportPortfolio> portfolios;

    public Portfolios(List<ExportPortfolio> portfolios) {
        this.portfolios = portfolios;
    }

    public List<ExportPortfolio> getPortfolios() {
        return portfolios;
    }
}

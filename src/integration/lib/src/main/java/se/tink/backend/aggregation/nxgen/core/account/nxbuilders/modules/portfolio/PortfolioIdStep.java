package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio;

public interface PortfolioIdStep<T> {
    /**
     * Sets the unique identifier of a {@link PortfolioModule}.
     *
     * <p>For an account with one portfolio this can be the account nr. For accounts with multiple
     * portfolios this can be the account nr combined with some static name/systemType/id only
     * applicable to this portfolio.
     *
     * @param identifier
     */
    CashValueStep<T> withUniqueIdentifier(String identifier);
}

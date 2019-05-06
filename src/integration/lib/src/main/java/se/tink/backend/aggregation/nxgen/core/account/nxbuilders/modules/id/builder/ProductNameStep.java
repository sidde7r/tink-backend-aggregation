package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder;

public interface ProductNameStep<T> {

    /**
     * Sets the product name of the account. This is typically what the bank calls this type of
     * account but can be more specific than the account type. e.g. "Corporate Loan Account",
     * "Investment Holdings Depot".
     *
     * @param productName The name of the product.
     * @return The next step of the builder.
     */
    IdentifierStep<T> setProductName(String productName);
}

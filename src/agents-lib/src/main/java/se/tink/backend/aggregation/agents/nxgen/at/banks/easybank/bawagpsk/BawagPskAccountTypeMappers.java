package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapperExecutor;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypePredicateMapper;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.libraries.pair.Pair;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class BawagPskAccountTypeMappers {
    private static final Logger logger = LoggerFactory.getLogger(BawagPskAccountTypeMappers.class);

    private AccountTypeMapper productCodeMapper;
    private AccountTypeMapper productTypeMapper;
    private AccountTypePredicateMapper<Product> productFallbackMapper;

    /**
     * It is assumed -- but not verified -- that the app infers the account type from the first
     * character of the account's product code. We cannot use <ProductType> to infer the account
     * type because it has been shown that the server incorrectly sets it to "CHECKING" even in
     * cases where it should be "SAVINGS".
     *
     * @return Optional.empty() if the product is not a transactional account (e.g. credit card,
     *     loan)
     */
    public Optional<AccountTypes> inferAccountType(
            final String productCode, final String productType) {
        // TODO refactor with isPresentOrElse when we are past Java 8

        final Optional<AccountTypes> accountTypeFromCode =
                getProductCodeMapper().translate(productCode);

        if (accountTypeFromCode.isPresent()) {
            return accountTypeFromCode;
        }

        final Optional<AccountTypes> accountTypeFromCodeFallback =
                getFallbackMapper()
                        .translate(
                                new BawagPskAccountTypeMappers.Product(productCode, productType));

        if (accountTypeFromCodeFallback.isPresent()) {
            return accountTypeFromCodeFallback;
        }

        final Optional<AccountTypes> accountTypeFromType =
                getProductTypeMapper().translate(productType);

        if (!accountTypeFromType.isPresent()) {
            logger.warn(
                    "{} Could not infer account type from product type \"{}\"; ignoring the account",
                    BawagPskConstants.LogTags.TRANSACTION_UNKNOWN_PRODUCT_TYPE.toTag(),
                    productType);
        }

        return accountTypeFromType;
    }

    public static final class Product {
        private final String productCode;
        private final String productType;

        public Product(final String productCode, final String productType) {
            this.productCode = productCode;
            this.productType = productType;
        }

        private String getProductCode() {
            return productCode;
        }

        private String getProductType() {
            return productType;
        }

        public static Predicate<Product> codeMatches(final String regex) {
            return t ->
                    t.getProductCode() != null
                            && Pattern.compile(regex).matcher(t.getProductCode()).matches();
        }

        @Override
        public String toString() {
            return String.format("Product(\"%s\", \"%s\")", productCode, productType);
        }
    }

    private class BawagPskAccountTypeMapperExecutor implements AccountTypeMapperExecutor<Product> {
        private final Logger logger =
                LoggerFactory.getLogger(BawagPskAccountTypeMapperExecutor.class);

        @Override
        public void onUnknownAccountType(final Product accountTypeKey) {
            logger.warn(
                    "Found unknown product code \"{}\" with product type \"{}\"",
                    accountTypeKey.getProductCode(),
                    accountTypeKey.getProductType());
        }

        @Override
        public void onUnambiguousPredicateMatch(
                final Product accountTypeKey,
                final Predicate<Product> matchingPredicate,
                final AccountTypes accountType) {
            final String message =
                    "Product code \"{}\" with product type \"{}\" was not explicitly associated with an account type."
                            + " Setting it to {} since a fallback predicate is associated with it.";
            logger.warn(
                    message,
                    accountTypeKey.getProductCode(),
                    accountTypeKey.getProductType(),
                    accountType);
        }

        @Override
        public void onAmbiguousPredicateMatch(
                final Product accountTypeKey,
                final List<Pair<Predicate<Product>, AccountTypes>> matches) {
            final String message =
                    "Product code \"{}\" with product type \"{}\" was not explicitly associated with an account type,"
                            + " and matched multiple predicates associated with different account types.";
            logger.warn(message, accountTypeKey.getProductCode(), accountTypeKey.getProductType());
        }
    }

    public AccountTypePredicateMapper<Product> getFallbackMapper() {
        if (productFallbackMapper == null) {
            productFallbackMapper =
                    AccountTypePredicateMapper.<Product>builder()
                            .setExecutor(new BawagPskAccountTypeMapperExecutor())
                            .fallbackValue(AccountTypes.CHECKING, Product.codeMatches("B\\w\\w\\w"))
                            .fallbackValue(AccountTypes.SAVINGS, Product.codeMatches("D\\w\\w\\w"))
                            .fallbackValue(
                                    AccountTypes.CREDIT_CARD, Product.codeMatches("00\\w\\w"))
                            .fallbackValue(AccountTypes.LOAN, Product.codeMatches("S\\w\\w\\w"))
                            .fallbackValue(AccountTypes.LOAN, Product.codeMatches("U\\w\\w\\w"))
                            .build();
        }
        return productFallbackMapper;
    }

    public AccountTypeMapper getProductCodeMapper() {
        if (productCodeMapper == null) {
            productCodeMapper =
                    AccountTypeMapper.builder()
                            .put(
                                    AccountTypes.CHECKING,
                                    "B100",
                                    "B101",
                                    "B111",
                                    "B113",
                                    "B114",
                                    "B120",
                                    "B121",
                                    "B131",
                                    "B132",
                                    "B133",
                                    "B300",
                                    "B400",
                                    "B410",
                                    "B420",
                                    "B460",
                                    "B510",
                                    "B512",
                                    "B531",
                                    "B553",
                                    "B600")
                            .put(
                                    AccountTypes.SAVINGS,
                                    "D242",
                                    "D250",
                                    "D253",
                                    "D256",
                                    "D260",
                                    "D263",
                                    "D264",
                                    "D267",
                                    "D268",
                                    "D270",
                                    "D272")
                            .put(
                                    AccountTypes.CREDIT_CARD,
                                    "00BD",
                                    "00EA",
                                    "00EC",
                                    "00EQ",
                                    "00ET",
                                    "00PD",
                                    "00PF")
                            .put(AccountTypes.LOAN, "S110", "S132", "U100", "U411")
                            .ignoreKeys(
                                    "T99A" // Product type "TIME_DEPOSIT"; not yet supported
                                    )
                            .build();
        }
        return productCodeMapper;
    }

    // Another fallback mapper; more error-prone because the bank assigns a savings account to
    // "CHECKING" for some reason
    public AccountTypeMapper getProductTypeMapper() {
        if (productTypeMapper == null) {
            productTypeMapper =
                    AccountTypeMapper.builder()
                            .put(AccountTypes.CHECKING, "CHECKING")
                            .put(AccountTypes.SAVINGS, "SAVINGS")
                            .put(AccountTypes.CREDIT_CARD, "CREDIT_CARD")
                            .put(AccountTypes.LOAN, "LOAN")
                            .build();
        }
        return productTypeMapper;
    }
}

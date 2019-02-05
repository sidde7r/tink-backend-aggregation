package se.tink.backend.aggregation.nxgen.core.account;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.pair.Pair;

public final class AccountTypePredicateMapperTest {

    private static class Product {
        private final String productCode;
        private final String productType;

        public Product(final String code, final String type) {
            productCode = code;
            productType = type;
        }

        private String getProductCode() {
            return productCode;
        }

        private String getProductType() {
            return productType;
        }
    }

    private static class Executor implements AccountTypeMapperExecutor<Product> {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void onUnknownAccountType(final Product accountTypeKey) {
            String message =
                    String.format(
                            "UNKNOWN: product code = %s, product type = %s",
                            accountTypeKey.getProductCode(), accountTypeKey.getProductType());
            messages.add(
                    String.format(
                            message,
                            accountTypeKey.getProductCode(),
                            accountTypeKey.getProductType()));
        }

        @Override
        public void onUnambiguousPredicateMatch(
                final Product accountTypeKey,
                final Predicate<Product> matchingPredicate,
                final AccountTypes accountType) {
            String message =
                    String.format(
                            "FALLBACK: product code = %s, product type = %s, account type = %s",
                            accountTypeKey.getProductCode(),
                            accountTypeKey.getProductType(),
                            accountType);
            messages.add(
                    String.format(
                            message,
                            accountTypeKey.getProductCode(),
                            accountTypeKey.getProductType(),
                            accountType));
        }

        @Override
        public void onAmbiguousPredicateMatch(
                final Product accountTypeKey,
                final List<Pair<Predicate<Product>, AccountTypes>> matches) {
            String message =
                    String.format(
                            "AMBIGUOUS: product code = %s, product type = %s",
                            accountTypeKey.getProductCode(), accountTypeKey.getProductType());
            messages.add(
                    String.format(
                            message,
                            accountTypeKey.getProductCode(),
                            accountTypeKey.getProductType()));
        }

        public List<String> getMessages() {
            return messages;
        }
    }

    @Test
    public void
            ensureTranslate_withMatchingSavingsFallback_returnsSavingsAndLogUnambiguousFallback() {
        final Executor executor = new Executor();

        final AccountTypePredicateMapper<Product> mapper =
                AccountTypePredicateMapper.<Product>builder()
                        .setExecutor(executor)
                        .fallbackValue(
                                AccountTypes.SAVINGS,
                                t ->
                                        Pattern.compile("S\\w\\w\\w")
                                                .matcher(t.getProductCode())
                                                .matches())
                        .build();

        final Optional<AccountTypes> returned = mapper.translate(new Product("S42C", "SAVINGS"));

        Assert.assertTrue(returned.isPresent());
        Assert.assertEquals(AccountTypes.SAVINGS, returned.get());
        Assert.assertEquals(1, executor.getMessages().size());
        Assert.assertThat(executor.getMessages().get(0), CoreMatchers.containsString("FALLBACK"));
    }

    @Test
    public void ensureTranslate_withMatchingAmbiguousFallbacks_logAmbiguousFallback() {
        final Executor executor = new Executor();

        final AccountTypePredicateMapper<Product> mapper =
                AccountTypePredicateMapper.<Product>builder()
                        .setExecutor(executor)
                        .fallbackValue(
                                AccountTypes.CHECKING,
                                t ->
                                        Pattern.compile("\\w\\w\\wC")
                                                .matcher(t.getProductCode())
                                                .matches())
                        .fallbackValue(
                                AccountTypes.SAVINGS,
                                t ->
                                        Pattern.compile("S\\w\\w\\w")
                                                .matcher(t.getProductCode())
                                                .matches())
                        .build();

        mapper.translate(new Product("S42C", "LOAN"));

        Assert.assertEquals(1, executor.getMessages().size());
        Assert.assertThat(executor.getMessages().get(0), CoreMatchers.containsString("AMBIGUOUS"));

        // The product type should also be in the logs
        Assert.assertThat(executor.getMessages().get(0), CoreMatchers.containsString("LOAN"));
    }

    @Test
    public void ensureTranslate_withSavingsStringMappedToSavingsAndRegex_returnsSavings() {
        final AccountTypePredicateMapper<Product> mapper =
                AccountTypePredicateMapper.<Product>builder()
                        .setExecutor(new Executor())
                        .fallbackValue(
                                AccountTypes.SAVINGS,
                                t ->
                                        Pattern.compile("S\\w\\w\\w")
                                                .matcher(t.getProductCode())
                                                .matches())
                        .build();

        final Optional<AccountTypes> returned = mapper.translate(new Product("S108", "SAVINGS"));

        Assert.assertTrue(returned.isPresent());
        Assert.assertEquals(AccountTypes.SAVINGS, returned.get());
    }

    @Test
    public void ensureTranslate_withSavingsStringMappedToRegexOnly_returnsSavings() {
        final AccountTypePredicateMapper<Product> mapper =
                AccountTypePredicateMapper.<Product>builder()
                        .setExecutor(new Executor())
                        .fallbackValue(
                                AccountTypes.SAVINGS,
                                t ->
                                        Pattern.compile("S\\w\\w\\w")
                                                .matcher(t.getProductCode())
                                                .matches())
                        .build();

        final Optional<AccountTypes> returned = mapper.translate(new Product("S023", "SAVINGS"));

        Assert.assertTrue(returned.isPresent());
        Assert.assertEquals(AccountTypes.SAVINGS, returned.get());
    }

    @Test
    public void ensureTranslate_withSavingsStringMappedToNeither_returnsEmpty() {
        final AccountTypePredicateMapper<Product> mapper =
                AccountTypePredicateMapper.<Product>builder()
                        .setExecutor(new Executor())
                        .fallbackValue(
                                AccountTypes.SAVINGS,
                                t ->
                                        Pattern.compile("S\\w\\w\\w")
                                                .matcher(t.getProductCode())
                                                .matches())
                        .build();

        final Optional<AccountTypes> returned =
                mapper.translate(new Product("D023", "CREDIT_CARD"));

        Assert.assertTrue(!returned.isPresent());
    }

    @Test
    public void ensureTranslate_withTwoSavingsRegexesOneMatch_returnsSavings() {
        final AccountTypePredicateMapper<Product> mapper =
                AccountTypePredicateMapper.<Product>builder()
                        .setExecutor(new Executor())
                        .fallbackValue(
                                AccountTypes.SAVINGS,
                                t ->
                                        Pattern.compile("S\\w\\w\\w")
                                                .matcher(t.getProductCode())
                                                .matches())
                        .fallbackValue(
                                AccountTypes.SAVINGS,
                                t ->
                                        Pattern.compile("s\\w\\w\\w")
                                                .matcher(t.getProductCode())
                                                .matches())
                        .build();

        final Optional<AccountTypes> returned = mapper.translate(new Product("S023", "SAVINGS"));

        Assert.assertTrue(returned.isPresent());
        Assert.assertEquals(AccountTypes.SAVINGS, returned.get());
    }

    @Test
    public void ensureTranslate_withTwoMatchingMappedToSavings_returnsSavings() {
        final AccountTypePredicateMapper<Product> mapper =
                AccountTypePredicateMapper.<Product>builder()
                        .setExecutor(new Executor())
                        .fallbackValue(
                                AccountTypes.SAVINGS,
                                t ->
                                        Pattern.compile("S\\w\\w\\w")
                                                .matcher(t.getProductCode())
                                                .matches())
                        .fallbackValue(
                                AccountTypes.SAVINGS,
                                t ->
                                        Pattern.compile("\\w\\w\\wS")
                                                .matcher(t.getProductCode())
                                                .matches())
                        .fallbackValue(
                                AccountTypes.LOAN,
                                t ->
                                        Pattern.compile("L\\w\\w\\w")
                                                .matcher(t.getProductCode())
                                                .matches())
                        .build();

        final Optional<AccountTypes> returned = mapper.translate(new Product("S42S", "SAVINGS"));

        Assert.assertTrue(returned.isPresent());
        Assert.assertEquals(AccountTypes.SAVINGS, returned.get());
    }

    @Test
    public void
            ensureTranslate_withTwoMatchingMappedToSavingsAndChecking_returnsSavingsOrChecking() {
        final AccountTypePredicateMapper<Product> mapper =
                AccountTypePredicateMapper.<Product>builder()
                        .setExecutor(new Executor())
                        .fallbackValue(
                                AccountTypes.SAVINGS,
                                t ->
                                        Pattern.compile("S\\w\\w\\w")
                                                .matcher(t.getProductCode())
                                                .matches())
                        .fallbackValue(
                                AccountTypes.CHECKING,
                                t ->
                                        Pattern.compile("\\w\\w\\wC")
                                                .matcher(t.getProductCode())
                                                .matches())
                        .fallbackValue(
                                AccountTypes.LOAN,
                                t ->
                                        Pattern.compile("L\\w\\w\\w")
                                                .matcher(t.getProductCode())
                                                .matches())
                        .build();

        final Optional<AccountTypes> returned = mapper.translate(new Product("S42C", "SAVINGS"));

        Assert.assertTrue(returned.isPresent());
        Assert.assertTrue(
                returned.get() == AccountTypes.CHECKING || returned.get() == AccountTypes.SAVINGS);
    }
}

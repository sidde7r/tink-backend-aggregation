package se.tink.backend.aggregation.workers.commands.migrations.implementations.banks.skandiabanken;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

public class DuplicateSafeBankIdSanitizerTest {

    private static final ImmutableMap<String, String> TRANSACTIONAL_ACCOUNT_EXPECTED_MAP =
            ImmutableMap.<String, String>builder()
                    .put("123", "123")
                    .put("123-123", "123")
                    .put("123-duplicate", "123-duplicate")
                    .put("123-123-duplicate", "123-duplicate")
                    .put("123-duplicate-1", "123-duplicate-1")
                    .put("123-123-duplicate-1", "123-duplicate-1")
                    .put("123-123-duplicate-12", "123-duplicate-12")
                    .build();

    private static final ImmutableMap<String, String> INVESTMENT_ACCOUNT_EXPECTED_MAP =
            ImmutableMap.<String, String>builder()
                    .put("123", "123")
                    .put("1.2-3", "123")
                    .put("123-duplicate", "123-duplicate")
                    .put("1.2-3-duplicate", "123-duplicate")
                    .put("123-duplicate-1", "123-duplicate-1")
                    .put("1.2-3-duplicate-1", "123-duplicate-1")
                    .put("1.2-3-duplicate-12", "123-duplicate-12")
                    .build();

    @Test
    public void ensureThat_transactionalAccountSanitization_handlesExpectedFormats() {

        TRANSACTIONAL_ACCOUNT_EXPECTED_MAP.forEach(
                (input, expectedResult) -> {
                    final String sanitizedBankId =
                            DuplicateSafeBankIdSanitizer.from(input)
                                    .getSanitizeTransactionalAccountBankId();

                    Assert.assertEquals(expectedResult, sanitizedBankId);
                });
    }

    @Test
    public void ensureThat_investmentAccountSanitization_handlesExpectedFormats() {

        INVESTMENT_ACCOUNT_EXPECTED_MAP.forEach(
                (input, expectedResult) -> {
                    final String sanitizedBankId =
                            DuplicateSafeBankIdSanitizer.from(input).getSanitizeInvestmentBankId();

                    Assert.assertEquals(expectedResult, sanitizedBankId);
                });
    }
}

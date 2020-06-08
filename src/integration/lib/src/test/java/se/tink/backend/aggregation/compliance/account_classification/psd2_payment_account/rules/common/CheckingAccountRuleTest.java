package se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.rules.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.result.Psd2PaymentAccountClassificationResult;
import se.tink.libraries.enums.MarketCode;

public class CheckingAccountRuleTest {
    @Test
    public void nonPsd2MarketIsNotApplicable() {
        CheckingAccountRule rule = new CheckingAccountRule();

        // Test all non-PSD2 markets.
        Arrays.stream(MarketCode.values())
                .filter(marketCode -> !Psd2Markets.PSD2_MARKETS.contains(marketCode))
                .map(this::prepareMockedProviderWithMarketCode)
                .forEach(
                        provider -> {
                            // The rule should not be applicable for this market.
                            assertThat(rule.isApplicable(provider)).isFalse();
                        });
    }

    private void testRuleForProviderAndAccount(
            Provider provider,
            Account account,
            Psd2PaymentAccountClassificationResult expectedResult) {
        CheckingAccountRule rule = new CheckingAccountRule();
        assertThat(rule.isApplicable(provider)).isTrue();

        Psd2PaymentAccountClassificationResult result = rule.classify(provider, account);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void checkingAccountInPsd2MarketShouldReturnPaymentAccount() {
        Account account = prepareMockedAccountWithType(AccountTypes.CHECKING);

        Psd2Markets.PSD2_MARKETS.stream()
                .map(this::prepareMockedProviderWithMarketCode)
                .forEach(
                        provider ->
                                testRuleForProviderAndAccount(
                                        provider,
                                        account,
                                        Psd2PaymentAccountClassificationResult
                                                .PSD2_PAYMENT_ACCOUNT));
    }

    @Test
    public void nonCheckingAccountInPsd2MarketShouldReturnUndetermined() {
        // Test all non-Checking account types for all psd2 markets.
        Arrays.stream(AccountTypes.values())
                .filter(accountType -> !accountType.equals(AccountTypes.CHECKING))
                .map(this::prepareMockedAccountWithType)
                .forEach(this::expectUndeterminedForAllPsd2MarketsWithAccount);
    }

    private void expectUndeterminedForAllPsd2MarketsWithAccount(Account account) {
        Psd2Markets.PSD2_MARKETS.stream()
                .map(this::prepareMockedProviderWithMarketCode)
                .forEach(
                        provider ->
                                testRuleForProviderAndAccount(
                                        provider,
                                        account,
                                        Psd2PaymentAccountClassificationResult
                                                .PSD2_UNDETERMINED_PAYMENT_ACCOUNT));
    }

    private Provider prepareMockedProviderWithMarketCode(MarketCode marketCode) {
        Provider provider = mock(Provider.class);
        when(provider.getMarket()).thenReturn(marketCode.toString());
        return provider;
    }

    private Account prepareMockedAccountWithType(AccountTypes type) {
        Account account = mock(Account.class);
        when(account.getType()).thenReturn(type);
        return account;
    }
}

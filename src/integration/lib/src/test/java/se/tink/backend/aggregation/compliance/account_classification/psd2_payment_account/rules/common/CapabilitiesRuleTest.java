package se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.rules.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.result.Psd2PaymentAccountClassificationResult;
import se.tink.libraries.enums.MarketCode;

public class CapabilitiesRuleTest {
    @Test
    public void nonPsd2MarketIsNotApplicable() {
        CapabilitiesRule rule = new CapabilitiesRule();

        // Test all non-PSD2 markets.
        Arrays.stream(MarketCode.values())
                .filter(marketCode -> !Psd2Markets.PSD2_MARKETS.contains(marketCode))
                .map(marketCode -> prepareMockedProvider(marketCode, true))
                .forEach(
                        provider -> {
                            // The rule should not be applicable for this market.
                            assertThat(rule.isApplicable(provider)).isFalse();
                        });
    }

    @Test
    public void openBankingProviderInPsd2MarketsShouldReturnPaymentAccount() {
        CapabilitiesRule rule = new CapabilitiesRule();

        Psd2Markets.PSD2_MARKETS.forEach(
                marketCode -> {
                    Provider provider = prepareMockedProvider(marketCode, true);

                    assertThat(rule.isApplicable(provider)).isTrue();

                    Psd2PaymentAccountClassificationResult result =
                            rule.classify(provider, new Account());

                    assertThat(result)
                            .isEqualTo(Psd2PaymentAccountClassificationResult.PSD2_PAYMENT_ACCOUNT);
                });
    }

    @Test
    public void nonOpenBankingWithUnknownCapabilitiesShouldReturnUndetermined() {
        CapabilitiesRule rule = new CapabilitiesRule();

        Provider provider = prepareMockedNonOpenBankingProvider();
        Account account = prepareMockedAccountWithCapabilities(AccountCapabilities.createDefault());

        Psd2PaymentAccountClassificationResult result = rule.classify(provider, account);

        assertThat(result)
                .isEqualTo(
                        Psd2PaymentAccountClassificationResult.PSD2_UNDETERMINED_PAYMENT_ACCOUNT);
    }

    @Test
    public void nonOpenBankingWithAllYesCapabilitiesShouldReturnPaymentAccount() {
        CapabilitiesRule rule = new CapabilitiesRule();

        Provider provider = prepareMockedNonOpenBankingProvider();

        AccountCapabilities capabilities = AccountCapabilities.createDefault();
        capabilities.setCanPlaceFunds(AccountCapabilities.Answer.YES);
        capabilities.setCanMakeDomesticTransfer(AccountCapabilities.Answer.YES);
        capabilities.setCanReceiveDomesticTransfer(AccountCapabilities.Answer.YES);
        capabilities.setCanWithdrawFunds(AccountCapabilities.Answer.YES);
        Account account = prepareMockedAccountWithCapabilities(capabilities);

        Psd2PaymentAccountClassificationResult result = rule.classify(provider, account);

        assertThat(result).isEqualTo(Psd2PaymentAccountClassificationResult.PSD2_PAYMENT_ACCOUNT);
    }

    @Test
    public void nonOpenBankingWithAnyNoCapabilitiesShouldReturnNonPaymentAccount() {
        CapabilitiesRule rule = new CapabilitiesRule();

        Provider provider = prepareMockedNonOpenBankingProvider();

        AccountCapabilities capabilities = AccountCapabilities.createDefault();
        capabilities.setCanPlaceFunds(AccountCapabilities.Answer.YES);
        capabilities.setCanMakeDomesticTransfer(AccountCapabilities.Answer.YES);
        capabilities.setCanReceiveDomesticTransfer(AccountCapabilities.Answer.YES);
        capabilities.setCanWithdrawFunds(AccountCapabilities.Answer.NO);
        Account account = prepareMockedAccountWithCapabilities(capabilities);

        Psd2PaymentAccountClassificationResult result = rule.classify(provider, account);

        assertThat(result)
                .isEqualTo(Psd2PaymentAccountClassificationResult.PSD2_NON_PAYMENT_ACCOUNT);
    }

    private Provider prepareMockedNonOpenBankingProvider() {
        // Sweden is a PSD2 market.
        return prepareMockedProvider(MarketCode.SE, false);
    }

    private Provider prepareMockedProvider(MarketCode marketCode, boolean isOpenBanking) {
        Provider provider = mock(Provider.class);
        when(provider.getMarket()).thenReturn(marketCode.toString());
        when(provider.isOpenBanking()).thenReturn(isOpenBanking);
        return provider;
    }

    private Account prepareMockedAccountWithCapabilities(AccountCapabilities capabilities) {
        Account account = mock(Account.class);
        when(account.getCapabilities()).thenReturn(capabilities);
        return account;
    }
}

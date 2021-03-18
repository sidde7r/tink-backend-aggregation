package se.tink.backend.aggregation.nxgen.core.account.investment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule.InstrumentType;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class InvestmentAccountBuilderTest {

    private static final IdModule ID_MODULE =
            IdModule.builder()
                    .withUniqueIdentifier("1234")
                    .withAccountNumber("134246685684")
                    .withAccountName("Account")
                    .addIdentifier(
                            AccountIdentifier.create(AccountIdentifierType.SE, "33009101010011"))
                    .build();

    private static final PortfolioModule PORTFOLIO_MODULE =
            PortfolioModule.builder()
                    .withType(PortfolioType.ISK)
                    .withUniqueIdentifier("1123")
                    .withCashValue(100d)
                    .withTotalProfit(50d)
                    .withTotalValue(130d)
                    .withInstruments(
                            InstrumentModule.builder()
                                    .withType(InstrumentType.FUND)
                                    .withId(InstrumentIdModule.of("SE0378331005", "SE", "name"))
                                    .withMarketPrice(5d)
                                    .withMarketValue(20d)
                                    .withAverageAcquisitionPrice(7d)
                                    .withCurrency("SEK")
                                    .withQuantity(20d)
                                    .withProfit(100d)
                                    .build())
                    .build();

    @Test(expected = NullPointerException.class)
    public void missingPortfolioArray() {
        InvestmentAccount.nxBuilder().withPortfolios((PortfolioModule) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyPortfolioArray() {
        InvestmentAccount.nxBuilder().withPortfolios();
    }

    @Test(expected = NullPointerException.class)
    public void missingPortfolioList() {
        InvestmentAccount.nxBuilder().withPortfolios((List<PortfolioModule>) null);
    }

    @Test(expected = NullPointerException.class)
    public void missingPortfolio() {
        List<PortfolioModule> portfolioModules = Lists.newArrayList();
        portfolioModules.add(null);
        InvestmentAccount.nxBuilder().withPortfolios(portfolioModules);
    }

    @Test(expected = NullPointerException.class)
    public void missingCashBalance() {
        InvestmentAccount.nxBuilder().withoutPortfolios().withCashBalance(null);
    }

    @Test(expected = NullPointerException.class)
    public void missingCurrencyCode() {
        InvestmentAccount.nxBuilder().withoutPortfolios().withZeroCashBalance(null);
    }

    @Test(expected = NullPointerException.class)
    public void missingIdModule() {
        InvestmentAccount.nxBuilder()
                .withoutPortfolios()
                .withCashBalance(ExactCurrencyAmount.of(BigDecimal.valueOf(200), "SEK"))
                .withId(null);
    }

    @Test
    public void buildWithoutPortfolios() {
        InvestmentAccount account =
                InvestmentAccount.nxBuilder()
                        .withoutPortfolios()
                        .withCashBalance(ExactCurrencyAmount.of(BigDecimal.valueOf(200), "SEK"))
                        .withId(ID_MODULE)
                        .build();

        assertEquals(AccountTypes.INVESTMENT, account.getType());
        assertTrue(account.isUniqueIdentifierEqual("1234"));
        assertEquals("134246685684", account.getAccountNumber());
        assertEquals("Account", account.getName());
        assertEquals(1, account.getIdentifiers().size());
        assertEquals(
                AccountIdentifier.create(AccountIdentifierType.SE, "33009101010011"),
                account.getIdentifiers().get(0));
        assertEquals(
                ExactCurrencyAmount.of(BigDecimal.valueOf(200), "SEK"), account.getExactBalance());
    }

    @Test
    public void buildWithPortfoliosArray() {
        InvestmentAccount account =
                InvestmentAccount.nxBuilder()
                        .withPortfolios(PORTFOLIO_MODULE)
                        .withCashBalance(ExactCurrencyAmount.of(BigDecimal.valueOf(200), "SEK"))
                        .withId(ID_MODULE)
                        .build();

        assertEquals(AccountTypes.INVESTMENT, account.getType());
        assertTrue(account.isUniqueIdentifierEqual("1234"));
        assertEquals("134246685684", account.getAccountNumber());
        assertEquals("Account", account.getName());
        assertEquals(1, account.getIdentifiers().size());
        assertEquals(
                AccountIdentifier.create(AccountIdentifierType.SE, "33009101010011"),
                account.getIdentifiers().get(0));
        assertEquals(
                ExactCurrencyAmount.of(BigDecimal.valueOf(330), "SEK"), account.getExactBalance());
        assertNotNull(account.getSystemPortfolios());
        assertTrue(account.getSystemPortfolios() instanceof ImmutableList);
        assertEquals(1, account.getSystemPortfolios().size());
        assertPortfolioEquals(
                PORTFOLIO_MODULE.toSystemPortfolio(), account.getSystemPortfolios().get(0));
    }

    @Test
    public void buildWithPortfoliosList() {
        InvestmentAccount account =
                InvestmentAccount.nxBuilder()
                        .withPortfolios(Lists.newArrayList(PORTFOLIO_MODULE))
                        .withCashBalance(ExactCurrencyAmount.of(BigDecimal.valueOf(200), "SEK"))
                        .withId(ID_MODULE)
                        .build();

        assertEquals(AccountTypes.INVESTMENT, account.getType());
        assertTrue(account.isUniqueIdentifierEqual("1234"));
        assertEquals("134246685684", account.getAccountNumber());
        assertEquals("Account", account.getName());
        assertEquals(1, account.getIdentifiers().size());
        assertEquals(
                AccountIdentifier.create(AccountIdentifierType.SE, "33009101010011"),
                account.getIdentifiers().get(0));
        assertEquals(
                ExactCurrencyAmount.of(BigDecimal.valueOf(330), "SEK"), account.getExactBalance());
        assertNotNull(account.getSystemPortfolios());
        assertTrue(account.getSystemPortfolios() instanceof ImmutableList);
        assertEquals(1, account.getSystemPortfolios().size());
        assertPortfolioEquals(
                PORTFOLIO_MODULE.toSystemPortfolio(), account.getSystemPortfolios().get(0));
    }

    @Test
    public void buildWithApiIdentifier() {
        InvestmentAccount account =
                InvestmentAccount.nxBuilder()
                        .withoutPortfolios()
                        .withCashBalance(ExactCurrencyAmount.of(BigDecimal.valueOf(200), "SEK"))
                        .withId(ID_MODULE)
                        .setApiIdentifier("65423-13445")
                        .build();

        assertEquals(AccountTypes.INVESTMENT, account.getType());
        assertTrue(account.isUniqueIdentifierEqual("1234"));
        assertEquals("134246685684", account.getAccountNumber());
        assertEquals("Account", account.getName());
        assertEquals(1, account.getIdentifiers().size());
        assertEquals(
                AccountIdentifier.create(AccountIdentifierType.SE, "33009101010011"),
                account.getIdentifiers().get(0));
        assertEquals(
                ExactCurrencyAmount.of(BigDecimal.valueOf(200), "SEK"), account.getExactBalance());
        assertEquals("65423-13445", account.getApiIdentifier());
    }

    @Test
    public void buildWithHolderName() {
        InvestmentAccount account =
                InvestmentAccount.nxBuilder()
                        .withoutPortfolios()
                        .withCashBalance(ExactCurrencyAmount.of(BigDecimal.valueOf(200), "SEK"))
                        .withId(ID_MODULE)
                        .setApiIdentifier("65423-13445")
                        .addHolderName("Account Holder Name")
                        .build();

        assertEquals(AccountTypes.INVESTMENT, account.getType());
        assertTrue(account.isUniqueIdentifierEqual("1234"));
        assertEquals("134246685684", account.getAccountNumber());
        assertEquals("Account", account.getName());
        assertEquals(1, account.getIdentifiers().size());
        assertEquals(
                AccountIdentifier.create(AccountIdentifierType.SE, "33009101010011"),
                account.getIdentifiers().get(0));
        assertEquals(
                ExactCurrencyAmount.of(BigDecimal.valueOf(200), "SEK"), account.getExactBalance());
        assertEquals("65423-13445", account.getApiIdentifier());
        assertEquals("Account Holder Name", account.getHolderName().toString());
    }

    @Test
    public void buildWithCalculatedBalance() {
        InvestmentAccount account =
                InvestmentAccount.nxBuilder()
                        .withPortfolios(PORTFOLIO_MODULE)
                        .withZeroCashBalance("SEK")
                        .withId(ID_MODULE)
                        .setApiIdentifier("65423-13445")
                        .addHolderName("Account Holder Name")
                        .build();

        assertEquals(AccountTypes.INVESTMENT, account.getType());
        assertTrue(account.isUniqueIdentifierEqual("1234"));
        assertEquals("134246685684", account.getAccountNumber());
        assertEquals("Account", account.getName());
        assertEquals(1, account.getIdentifiers().size());
        assertEquals(
                AccountIdentifier.create(AccountIdentifierType.SE, "33009101010011"),
                account.getIdentifiers().get(0));
        assertEquals(
                ExactCurrencyAmount.of(BigDecimal.valueOf(130), "SEK"), account.getExactBalance());
        assertEquals("65423-13445", account.getApiIdentifier());
        assertEquals("Account Holder Name", account.getHolderName().toString());
    }

    @Test
    public void buildWithCashBalance() {
        InvestmentAccount account =
                InvestmentAccount.nxBuilder()
                        .withPortfolios(
                                PortfolioModule.builder()
                                        .withType(PortfolioType.ISK)
                                        .withUniqueIdentifier("52010456235")
                                        .withCashValue(200)
                                        .withTotalProfit(20.3)
                                        .withTotalValue(130)
                                        .withInstruments(
                                                InstrumentModule.builder()
                                                        .withType(InstrumentType.FUND)
                                                        .withId(
                                                                InstrumentIdModule.of(
                                                                        "SE0378331005",
                                                                        "SE",
                                                                        "name"))
                                                        .withMarketPrice(10)
                                                        .withMarketValue(20)
                                                        .withAverageAcquisitionPrice(1d)
                                                        .withCurrency("SEK")
                                                        .withQuantity(50)
                                                        .withProfit(20.3)
                                                        .build())
                                        .build())
                        .withCashBalance(ExactCurrencyAmount.of(BigDecimal.valueOf(100), "SEK"))
                        .withId(ID_MODULE)
                        .setApiIdentifier("65423-13445")
                        .addHolderName("Account Holder Name")
                        .build();

        assertEquals(AccountTypes.INVESTMENT, account.getType());
        assertTrue(account.isUniqueIdentifierEqual("1234"));
        assertEquals("134246685684", account.getAccountNumber());
        assertEquals("Account", account.getName());
        assertEquals(1, account.getIdentifiers().size());
        assertEquals(
                AccountIdentifier.create(AccountIdentifierType.SE, "33009101010011"),
                account.getIdentifiers().get(0));
        assertEquals(
                ExactCurrencyAmount.of(BigDecimal.valueOf(230), "SEK"), account.getExactBalance());
        assertEquals("65423-13445", account.getApiIdentifier());
        assertEquals("Account Holder Name", account.getHolderName().toString());
    }

    @Test
    public void storageTest() {
        SomeBoxing box = new SomeBoxing("TestString", 15);
        InvestmentAccount account =
                InvestmentAccount.nxBuilder()
                        .withoutPortfolios()
                        .withCashBalance(ExactCurrencyAmount.of(BigDecimal.valueOf(200), "SEK"))
                        .withId(ID_MODULE)
                        .putInTemporaryStorage("box", box)
                        .build();

        Optional<SomeBoxing> storage = account.getFromTemporaryStorage("box", SomeBoxing.class);

        assertTrue(storage.isPresent());
        assertEquals("TestString", storage.get().x);
        assertEquals(15, storage.get().y);
    }

    private static void assertPortfolioEquals(Portfolio p1, Portfolio p2) {
        assertEquals(p1.getUniqueIdentifier(), p2.getUniqueIdentifier());
        assertEquals(p1.getTotalProfit(), p2.getTotalProfit());
        assertEquals(p1.getCashValue(), p2.getCashValue());
        assertEquals(p1.getTotalValue(), p2.getTotalValue());
        assertEquals(p1.getType(), p2.getType());
        assertEquals(p1.getRawType(), p2.getRawType());
        assertEquals(p1.getInstruments().size(), p2.getInstruments().size());
    }

    @SuppressWarnings("unused")
    private static class SomeBoxing {
        private String x;

        private int y;

        private SomeBoxing() {}

        SomeBoxing(String x, int y) {
            this.x = x;
            this.y = y;
        }

        public String getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}

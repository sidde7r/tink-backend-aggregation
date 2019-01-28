package se.tink.backend.aggregation.nxgen.agents.demo.demogenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoInvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.libraries.amount.Amount;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;

public class InvestmentGenerator {

    public static Collection<InvestmentAccount> fetchInvestmentAccounts(String currency, DemoInvestmentAccount accountDefinition) {
        List<InvestmentAccount> investmentAccounts = new ArrayList<>();
        if (Objects.isNull(accountDefinition)) {
            return investmentAccounts;
        }

        investmentAccounts.add(
                InvestmentAccount.builder(accountDefinition.getAccountId())
                        .setBalance(new Amount(currency,  DemoConstants.getSekToCurrencyConverter(
                                currency,accountDefinition.getAccountBalance())))
                        .setName("")
                        .setAccountNumber(accountDefinition.getAccountId())
                        .setPortfolios(InvestmentGenerator.generateFakePortfolios(
                                accountDefinition.getAccountId(),
                                accountDefinition.getAccountBalance()))
                        .setCashBalance(new Amount(currency,
                                accountDefinition.getAccountBalance()))
                        .build()
        );

        return investmentAccounts;
    }

    private static List<Portfolio> generateFakePortfolios (String accountId, double balance) {
        ArrayList<Portfolio> portfolios = new ArrayList<>();
        portfolios.add(generateFakePortolio(accountId, balance/2,  Portfolio.Type.ISK));
        portfolios.add(generateFakePortolio(accountId, balance/2,  Portfolio.Type.DEPOT));

        return portfolios;
    }

    private static Portfolio generateFakePortolio(String accountId, double balance, Portfolio.Type type) {
        Portfolio portfolio = new Portfolio();
        portfolio.setType(type);
        portfolio.setCashValue(balance);
        portfolio.setUniqueIdentifier(accountId);
        portfolio.setTotalValue(balance);
        List<Instrument> instruments = new ArrayList<>();
        if (type == Portfolio.Type.ISK) {
            instruments.add(createFakeInstrument("SE0009778954", "OMXS", "SEK", "XACT högutdelande", "XACTHDIV",
                    Instrument.Type.OTHER, "ETF"));
            instruments.add(createFakeInstrument("IE00BZ0PKV06", "LONDON STOCK EXCHANGE", "EUR",
                    "iShares Edge MSCI Europe Multifactor UCITS ETF EUR", "IFSE", Instrument.Type.OTHER, "ETF"));
            instruments.add(createFakeInstrument("NO0010257801", "OSLO BORS", "NOK",
                    "DNB OBX", "OBXEDNB", Instrument.Type.OTHER, "ETF"));
            instruments.add(createFakeInstrument("DK0060830644", "OMX NORDIC EQUITIES", "DKK",
                    "BEAR SX5E X15 NORDNET", "BEAR SX5E X15 NORDNET", Instrument.Type.OTHER, "ETN"));
            instruments.add(createFakeInstrument("US06742W4309", "NYSE ARCA", "USD",
                    "Barclays Women in Leadership ETN", "WIL", Instrument.Type.OTHER, "ETN"));
            instruments.add(createFakeInstrument("SE0000869646", "OMXS", "SEK",
                    "Boliden", "BOL", Instrument.Type.STOCK, "Aktie"));
        } else {
            instruments.add(createFakeInstrument("FI4000074984", "OMX Helsinki", "EUR",
                    "Valmet", "VALMT", Instrument.Type.STOCK, "Aktie"));
            instruments.add(createFakeInstrument("NO0010096985", "OBX Top 25", "NOK",
                    "Statoil", "STL", Instrument.Type.STOCK, "Aktie"));
            instruments.add(createFakeInstrument("DK0010181759", "OMXC", "DKK",
                    "Carlsberg B", "CARL B", Instrument.Type.STOCK, "Aktie"));
            instruments.add(createFakeInstrument("US0378331005", "NASDAQ", "USD",
                    "Apple", "AAPL", Instrument.Type.STOCK, "Aktie"));
            instruments.add(createFakeInstrument("SE0005798329", null, "SEK",
                    "Spiltan Högräntefond", null, Instrument.Type.FUND, "Fond"));
            instruments.add(createFakeInstrument("SE0001718388", null, "SEK",
                    "AVANZA ZERO", null, Instrument.Type.FUND, "Fond"));
        }

        portfolio.setInstruments(instruments);
        return portfolio;
    }

    private static Instrument createFakeInstrument(String ISIN, String marketplace, String currency, String name,
            String ticker, Instrument.Type type, String rawType) {
        Instrument instrument = new Instrument();
        instrument.setCurrency(currency);
        instrument.setIsin(ISIN);
        instrument.setMarketPlace(marketplace);
        instrument.setUniqueIdentifier(ISIN + marketplace);
        instrument.setAverageAcquisitionPrice(123.45);
        instrument.setQuantity(2.00);
        // Value of instrument has increased by 100% since purchase
        instrument.setPrice(123.45 * 2);
        instrument.setMarketValue(instrument.getQuantity() * instrument.getPrice());
        instrument.setName(name);
        instrument.setProfit(instrument.getMarketValue() - instrument.getAverageAcquisitionPrice());
        instrument.setTicker(ticker);
        instrument.setType(type);
        instrument.setRawType(rawType);
        return instrument;
    }
}

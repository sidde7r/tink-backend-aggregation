package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.depot;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.accounts.SEPAAccount;

public class MT535Statement {

    private final SEPAAccount account;
    private final List<String> segments;
    private static final String MT535_FIN_SEGMENT_START = ":16R:FIN";

    public MT535Statement(SEPAAccount account, List<String> segments) {
        this.account = account;
        this.segments = segments;
    }

    public Portfolio toTinkPortfolio() {
        Portfolio portfolio = new Portfolio();
        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setUniqueIdentifier(account.getAccountNo());

        List<String> finSegs =
                segments.stream()
                        .filter(finSeg -> finSeg.startsWith(MT535_FIN_SEGMENT_START))
                        .collect(Collectors.toList());
        List<Instrument> instruments =
                finSegs.stream()
                        .map(MT535Instrument::new)
                        .map(MT535Instrument::toTinkInstrument)
                        .collect(Collectors.toList());
        portfolio.setInstruments(instruments);

        Double totalProfit =
                instruments.stream().map(Instrument::getProfit).reduce(0.0, Double::sum);
        portfolio.setTotalProfit(totalProfit);

        Double totalValue =
                instruments.stream().map(Instrument::getMarketValue).reduce(0.0, Double::sum);
        portfolio.setTotalValue(totalValue);

        return portfolio;
    }
}

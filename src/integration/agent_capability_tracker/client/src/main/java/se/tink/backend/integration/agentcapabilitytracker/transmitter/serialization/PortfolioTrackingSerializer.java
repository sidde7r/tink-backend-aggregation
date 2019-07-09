package se.tink.backend.integration.agentcapabilitytracker.transmitter.serialization;

import java.util.Collections;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Portfolio;

public class PortfolioTrackingSerializer extends TrackingMapSerializer {

    public static final String PORTFOLIO = "Portfolio";
    final Portfolio portfolio;

    public PortfolioTrackingSerializer(Portfolio portfolio) {
        super(String.format(PORTFOLIO + "<%s>", String.valueOf(portfolio.getType())));

        this.portfolio = portfolio;

        Optional.of(portfolio)
                .map(Portfolio::getInstruments)
                .orElseGet(Collections::emptyList)
                .forEach(
                        instrument ->
                                addChild(
                                        "instruments",
                                        new InstrumentTrackingSerializer(instrument)));
    }

    @Override
    protected TrackingList populateTrackingMap(TrackingList.Builder listBuilder) {
        listBuilder
                .putRedacted("uniqueIdentifier", portfolio.getUniqueIdentifier())
                .putRedacted("rawType", portfolio.getRawType())
                .putRedacted("totalProfit", portfolio.getTotalProfit())
                .putRedacted("cashValue", portfolio.getCashValue())
                .putRedacted("totalValue", portfolio.getTotalValue())
                .putListed("type", portfolio.getType());

        return listBuilder.build();
    }
}

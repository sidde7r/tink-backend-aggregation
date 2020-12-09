package se.tink.backend.integration.agent_data_availability_tracker.serialization;

import java.util.Collections;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.integration.agent_data_availability_tracker.common.TrackingList;
import se.tink.backend.integration.agent_data_availability_tracker.common.TrackingMapSerializer;

public class PortfolioTrackingSerializer extends TrackingMapSerializer {

    private static final String PORTFOLIO_ENTITY_NAME = "Portfolio";
    final Portfolio portfolio;

    public PortfolioTrackingSerializer(Portfolio portfolio) {
        super(String.format(PORTFOLIO_ENTITY_NAME + "<%s>", String.valueOf(portfolio.getType())));

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

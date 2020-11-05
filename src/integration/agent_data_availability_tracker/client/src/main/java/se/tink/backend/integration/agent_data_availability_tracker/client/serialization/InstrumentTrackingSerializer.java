package se.tink.backend.integration.agent_data_availability_tracker.client.serialization;

import se.tink.backend.aggregation.agents.models.Instrument;

public class InstrumentTrackingSerializer extends TrackingMapSerializer {

    private static final String INSTRUMENT_ENTITY_NAME = "Instrument";
    private final Instrument instrument;

    public InstrumentTrackingSerializer(Instrument instrument) {
        super(String.format(INSTRUMENT_ENTITY_NAME + "<%s>", String.valueOf(instrument.getType())));
        this.instrument = instrument;
    }

    @Override
    protected TrackingList populateTrackingMap(TrackingList.Builder listBuilder) {
        listBuilder
                .putRedacted("uniqueIdentifier", instrument.getUniqueIdentifier())
                .putRedacted("isin", instrument.getIsin())
                .putRedacted("marketPlace", instrument.getMarketPlace())
                .putRedacted("ticker", instrument.getTicker())
                .putRedacted("name", instrument.getName())
                .putRedacted("rawType", instrument.getRawType())
                .putRedacted("averageAcquisitionPrice", instrument.getAverageAcquisitionPrice())
                .putRedacted("marketValue", instrument.getMarketValue())
                .putRedacted("price", instrument.getPrice())
                .putRedacted("quantity", instrument.getQuantity())
                .putRedacted("profit", instrument.getProfit())
                .putListed("type", instrument.getType())
                .putListed("currency", instrument.getCurrency());

        return listBuilder.build();
    }
}

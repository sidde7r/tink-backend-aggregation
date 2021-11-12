package se.tink.backend.aggregation.agents.contexts;

import se.tink.backend.aggregation.nxgen.raw_data_events.event_producers.RawBankDataEventAccumulator;
import se.tink.libraries.credentials.service.RefreshableItem;

public interface RawBankDataEventContext {

    void setRawBankDataEventAccumulator(RawBankDataEventAccumulator rawBankDataEventAccumulator);

    RawBankDataEventAccumulator getRawBankDataEventAccumulator();

    void setCurrentRefreshableItemInProgress(RefreshableItem refreshableItem);

    RefreshableItem getCurrentRefreshableItemInProgress();
}

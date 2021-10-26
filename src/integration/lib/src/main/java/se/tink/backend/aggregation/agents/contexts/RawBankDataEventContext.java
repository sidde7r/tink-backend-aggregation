package se.tink.backend.aggregation.agents.contexts;

import se.tink.backend.aggregation.nxgen.http.event.event_producers.RawBankDataEventAccumulator;
import se.tink.libraries.credentials.service.RefreshableItem;

public interface RawBankDataEventContext {

    void setRawBankDataEventAccumulator(RawBankDataEventAccumulator rawBankDataEventAccumulator);

    RawBankDataEventAccumulator getRawBankDataEventAccumulator();

    void setCurrentRefreshableItemInProgress(RefreshableItem refreshableItem);

    RefreshableItem getCurrentRefreshableItemInProgress();
}

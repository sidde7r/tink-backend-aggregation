package se.tink.backend.aggregation.agents.summary.refresh;

import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import se.tink.libraries.credentials.service.RefreshableItem;

@AllArgsConstructor
@Getter(AccessLevel.PACKAGE)
@Setter(AccessLevel.PACKAGE)
class RefreshableItemSummary {

    private RefreshableItem item;
    private RefreshableItemFetchingStatus fetchingStatus;
    private List<Integer> fetched;
    private LocalDate oldestTransactionDate;
}

package se.tink.agent.sdk.fetching.transactions.paginators.index;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class IndexPaginationConfiguration {
    @Builder.Default private final int startIndex = 0;
}

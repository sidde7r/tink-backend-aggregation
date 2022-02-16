package se.tink.agent.sdk.fetching.transactions.paginators.date;

import java.time.Duration;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DatePaginationConfiguration {
    @Builder.Default private final Duration fetchWindow = Duration.ofDays(89);
    @Builder.Default private final int consecutiveEmptyPagesLimit = 4;
}

package se.tink.libraries.credentials.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.agents.rpc.FinancialService.FinancialServiceSegment;

/*
 * Class represents the scope of refresh operation.
 *
 * Context: currently refresh is limited by multiple factors specified independently, to name a few:
 * - Refreshable Items
 * - Opt-in etc.
 * This class is to encapsulate them all in unified manner.
 * Naming follows https://book-of.tink.network/engineering/product/api_design/#must-use-mongodb-style-filtering-for-list
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefreshScope {
    private Set<FinancialServiceSegment> financialServiceSegmentsIn;
    private TransactionsRefreshScope transactions;
    private Set<RefreshableItem> refreshableItemsIn;
}

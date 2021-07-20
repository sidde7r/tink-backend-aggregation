package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.transactions.dto.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.common.dto.responses.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.common.dto.responses.PageInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@Getter
public class TransactionsResponse implements PaginatorResponse {
    private List<TransactionsEntity> transactions;

    // one of below might be filled depending on the bank
    private List<LinksEntity> links;
    private PageInfoEntity pageInfo;

    @JsonIgnore @Setter PolishApiConstants.Transactions.TransactionTypeRequest typeRequest;

    public String getNextPage() {
        if (CollectionUtils.isNotEmpty(links)) {
            return links.get(0).getHref();
        } else if (pageInfo != null) {
            return pageInfo.getNextPage();
        }
        return null;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream()
                .map(transactionsEntity -> transactionsEntity.toTinkTransaction(typeRequest))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }
}

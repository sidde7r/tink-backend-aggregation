package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.rpc.NordeaResponseBase;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.NordeaTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.entities.TransactionsResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse extends NordeaResponseBase {
    private TransactionsResponseEntity response;

    private NordeaTransactionParser transactionParser;

    public TransactionKeyPaginatorResponse<LinkEntity> getPaginatorResponse(NordeaTransactionParser transactionParser) {
        return new NordeaTransactionPaginatorResponse(transactionParser, response);
    }

    public static class NordeaTransactionPaginatorResponse implements TransactionKeyPaginatorResponse<LinkEntity> {
        private TransactionsResponseEntity response;
        private NordeaTransactionParser transactionParser;

        private NordeaTransactionPaginatorResponse(NordeaTransactionParser transactionParser,
                TransactionsResponseEntity response) {
            this.transactionParser = transactionParser;
            this.response = response;
        }

        @Override
        public LinkEntity nextKey() {
            if (response == null || response.getContinuationKey() == null) {
                return null;
            }

            return response.findLinkByName(NordeaBaseConstants.Link.NEXT_LINK)
                    .orElse(null);
        }

        @Override
        public Collection<? extends Transaction> getTinkTransactions() {
            if (response == null) {
                return  Collections.emptyList();
            }

            return Optional.ofNullable(response.getTransactions()).orElse(Collections.emptyList()).stream()
                    .map(this.transactionParser::toTinkTransaction)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        }

        @Override
        public Optional<Boolean> canFetchMore() {
            return Optional.of(nextKey() != null);
        }
    }
}

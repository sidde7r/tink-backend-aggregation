package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.ListUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.LinkRelations;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.shared.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.transaction.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class FetchTransactionsResponse implements TransactionKeyPaginatorResponse<String> {

    @JsonProperty("items")
    private List<TransactionEntity> transactions;

    private List<LinkEntity> links;

    @JsonIgnore
    private Transaction toTinkTransaction(final TransactionEntity transaction) {
        return Transaction.builder()
                .setAmount(transaction.getAmount())
                .setDate(transaction.getDate())
                .setDescription(transaction.getSubtype())
                .setPending(transaction.isPending())
                .build();
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream().map(this::toTinkTransaction).collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(ListUtils.emptyIfNull(links)).map(this::isNextPageTokenPresent);
    }

    @Override
    public String nextKey() {
        return links.stream()
                .filter(link -> LinkRelations.NEXT.equalsIgnoreCase(link.getRelation()))
                .findFirst()
                .map(this::extractNextPageToken)
                .orElseThrow(
                        () -> new IllegalStateException(ErrorMessages.MISSING_NEXT_PAGE_TOKEN));
    }

    private String extractNextPageToken(LinkEntity link) {
        try {
            return URLEncodedUtils.parse(new URI(link.getReference()), "UTF-8").stream()
                    .filter(p -> p.getName().equals(QueryKeys.NEXT_PAGE_TOKEN))
                    .findFirst()
                    .map(NameValuePair::getValue)
                    .orElseThrow(
                            () ->
                                    new IllegalStateException(
                                            ErrorMessages.NEXT_PAGE_TOKEN_EXTRACT_FAILED));
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private Boolean isNextPageTokenPresent(List<LinkEntity> links) {
        return links.stream()
                .anyMatch(linkEntity -> linkEntity.getRelation().equals(LinkRelations.NEXT));
    }
}

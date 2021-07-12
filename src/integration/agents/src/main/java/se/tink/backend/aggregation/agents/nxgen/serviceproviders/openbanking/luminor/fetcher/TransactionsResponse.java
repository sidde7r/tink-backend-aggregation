package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities.BookedEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@Getter
public class TransactionsResponse implements PaginatorResponse {
    private AccountEntity account;

    @JsonProperty("booked")
    private List<BookedEntity> booked;

    @JsonProperty("_links")
    private LinksEntity links;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Optional.ofNullable(booked).orElse(Collections.emptyList()).stream()
                .map(BookedEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }

    @JsonIgnore
    public String getAccountHolderName() {
        if (Optional.ofNullable(booked).orElse(Collections.emptyList()).stream()
                .findFirst()
                .isPresent()) {
            return booked.stream().findFirst().get().getDebtorName();
        }
        return null;
    }
}

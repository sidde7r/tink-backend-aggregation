package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsLinksEntity {

    private LinkHrefType account;
    private LinkHrefType first;
    private LinkHrefType last;
    private LinkHrefType next;
    private LinkHrefType transactionDetails;

    public Optional<String> getAccount() {
        return getUrlFromHrefType(account);
    }

    public Optional<String> getFirst() {
        return getUrlFromHrefType(first);
    }

    public Optional<String> getLast() {
        return getUrlFromHrefType(last);
    }

    public Optional<String> getNext() {
        return getUrlFromHrefType(next);
    }

    private Optional<String> getUrlFromHrefType(LinkHrefType hrefType) {
        return Optional.ofNullable(hrefType).map(v -> v.getHref());
    }

    public Optional<String> getTransactionDetails() {
        return getUrlFromHrefType(transactionDetails);
    }

    public boolean hasNextLink() {
        return Optional.ofNullable(next).isPresent();
    }
}

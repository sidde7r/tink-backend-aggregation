package se.tink.backend.aggregation.agents.standalone.entity;

import com.google.common.base.Strings;
import java.util.Optional;

public class TransactionLinksEntity {

    private String viewAccount;
    private String first;
    private String next;
    private String last;

    public String getViewAccount() {
        return viewAccount;
    }

    public void setViewAccount(String viewAccount) {
        this.viewAccount = viewAccount;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public String getNextKey() {
        return Optional.ofNullable(next)
                .orElseThrow(() -> new IllegalStateException("Missing pagination key"));
    }

    public Boolean canFetchMore() {
        return !Strings.isNullOrEmpty(next);
    }
}

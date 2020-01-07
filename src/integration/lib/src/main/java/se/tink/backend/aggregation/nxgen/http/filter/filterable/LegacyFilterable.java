package se.tink.backend.aggregation.nxgen.http.filter.filterable;

import se.tink.backend.aggregation.nxgen.http.filter.Filter;

/*
 * A {@link Filter} instance MUST be occur at most once in any {@link Filterable} instance,
 * otherwise unexpected results may occur. If it is necessary to add the same type of {@link ClientFilter} more than once to
 * the same {@link Filterable} instance or to more than one {@link Filterable} instance then a new instance of that {@link
 * ClientFilter} MUST be added.
 *
 * @deprecated Use {@link NextGenFilterable} instead.
 */
@Deprecated
public abstract class LegacyFilterable<F extends Filterable> implements Filterable<F> {
    private Filter head;
    private Filter tail;

    public LegacyFilterable() {
        head = null;
        tail = null;
    }

    public LegacyFilterable(LegacyFilterable that) {
        // Cut the tail off of the one we copy from. This is because it is possible to add
        // "temporary" filters
        // to requests that we do NOT want to affect the main chain (which is attached to the
        // client).
        that.cutFilterTail();
        this.head = that.head;
        this.tail = that.tail;
    }

    public Filter getFilterHead() {
        return head;
    }

    public void cutFilterTail() {
        if (tail == null) {
            return;
        }
        tail.setNext(null);
    }

    public F addFilter(Filter filter) {
        if (isFilterPresent(filter)) {
            throw new IllegalStateException("Cannot add the same filter twice!");
        }

        // always add to the tail
        if (head == null) {
            head = filter;
            tail = filter;
            return (F) this;
        }
        tail.setNext(filter);
        tail = filter;
        return (F) this;
    }

    public F removeFilter(Filter filter) {
        if (head == null || filter == null) {
            return (F) this;
        }

        if (head == filter && tail == filter) {
            // This means there was only one filter. After it has been removed the list is empty.
            head = null;
            tail = null;
            return (F) this;
        }

        if (head == filter) {
            // This means that the filter we are about to remove is the first one in the chain.
            // Re-link the head.
            head = head.getNext();
            return (F) this;
        }

        for (Filter f = head; f != null; f = f.getNext()) {
            if (f.getNext() == filter) {

                if (filter == tail) {
                    // This means that the filter we are about to remove is the last filter.
                    // The previous filter (f) is now the end of the chain.
                    tail = f;
                }

                // relink chain
                f.setNext(f.getNext().getNext());
                break;
            }
        }
        return (F) this;
    }

    public boolean isFilterPresent(Filter filter) {
        for (Filter f = head; f != null && f != tail; f = f.getNext()) {
            if (f == filter) {
                return true;
            }
        }
        return false;
    }
}

package se.tink.backend.aggregation.nxgen.http.filter;

import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;

/*
    Example:
    class TestFilter extends Filter {
        public HttpResponse handle(HttpRequest httpRequest) throws HttpClientException, HttpRequestException {
            return nextFilter(httpRequest);
        }
    }

 * A filter is re-entrant and may be called by by more than one thread
 * at the same time.
 * <p>
 * A filter instance MUST be occur at most once in any {@link Filterable}
 * instance, otherwise unexpected results may occur.
 * If it is necessary to add the same type of client filter more than once
 * to the same {@link Filterable} instance or to more than one {@link Filterable}
 * instance then a new instance of that filter MUST be added.
 */
public abstract class Filter {
    private Filter next;

    /* package */ void setNext(Filter next) {
        this.next = next;
    }

    /* package */ final Filter getNext() {
        return next;
    }

    protected HttpResponse nextFilter(HttpRequest httpRequest) {
        return getNext().handle(httpRequest);
    }

    public abstract HttpResponse handle(HttpRequest httpRequest) throws HttpClientException, HttpResponseException;
}

package se.tink.backend.aggregation.nxgen.http.log;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.ClientFilter;
import se.tink.backend.aggregation.agents.HttpLoggableExecutor;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.http.filter.ClientFilterFactory;
import se.tink.backend.aggregation.utils.StringMasker;

/**
 * The reason for this abstraction is that a single filter instance cannot be attached to multiple
 * clients. Thus we need to be able to construct same kind of instances of filters on demand when
 * attaching to clients. This helps out storing that info and removing them when done.
 */
public class HttpLoggingFilterFactory implements ClientFilterFactory {
    private final AggregationLogger log;
    private final String logTag;
    private final Iterable<StringMasker> stringMaskers;
    private final Class<? extends HttpLoggableExecutor> agentClass;
    private final Multimap<Client, ClientFilter> createdFilters;

    public HttpLoggingFilterFactory(
            AggregationLogger log,
            String logTag,
            Iterable<StringMasker> stringMaskers,
            Class<? extends HttpLoggableExecutor> agentClass) {
        this.log = log;
        this.logTag = logTag;
        this.stringMaskers = stringMaskers;
        this.agentClass = agentClass;
        this.createdFilters = MultimapBuilder.hashKeys().arrayListValues().build();
    }

    @Override
    public ClientFilter addClientFilter(Client client) {
        HttpLoggingFilter filter = new HttpLoggingFilter(log, logTag, stringMaskers, agentClass);

        client.addFilter(filter);
        createdFilters.put(client, filter);

        return filter;
    }

    @Override
    public void removeClientFilters() {
        for (Client client : createdFilters.keySet()) {
            for (ClientFilter clientFilter : createdFilters.get(client)) {
                client.removeFilter(clientFilter);
            }
        }

        createdFilters.clear();
    }
}

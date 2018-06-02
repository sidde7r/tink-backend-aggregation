package se.tink.backend.common.search;

import java.util.concurrent.atomic.AtomicReference;

import org.elasticsearch.client.Client;

public class SearchProxy {

    private static AtomicReference<SearchProxy> instance = new AtomicReference<>();
    private Client client;

    private SearchProxy() {
        // nothing
    }

    public static SearchProxy getInstance() {
        if (instance.get() == null) {
            instance.compareAndSet(null, new SearchProxy());
        }
        
        return instance.get();
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

}

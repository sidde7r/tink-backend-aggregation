package se.tink.backend.aggregation.log;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.ClientFilter;

public interface ClientFilterFactory {
    ClientFilter addClientFilter(Client client);
    void removeClientFilters();
}

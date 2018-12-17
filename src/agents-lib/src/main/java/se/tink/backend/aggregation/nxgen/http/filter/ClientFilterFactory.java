package se.tink.backend.aggregation.nxgen.http.filter;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.ClientFilter;

public interface ClientFilterFactory {
    ClientFilter addClientFilter(Client client);
    void removeClientFilters();
}

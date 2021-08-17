package se.tink.libraries.http.client;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;

public class HstsFilter implements ContainerResponseFilter {

    private static final String HSTS = "Strict-Transport-Security";
    private static final String MAX_AGE = "max-age";
    private static final String INCLUDE_SUB_DOMAINS = "includeSubDomains";

    private static final int DEFAULT_MAX_AGE = 31536000;

    private final int maxAge;
    private final boolean includeSubDomains;

    public HstsFilter() {
        this(DEFAULT_MAX_AGE, Boolean.TRUE);
    }

    public HstsFilter(int maxAge, boolean includeSubDomains) {
        this.maxAge = maxAge;
        this.includeSubDomains = includeSubDomains;
    }

    @Override
    public ContainerResponse filter(
            ContainerRequest containerRequest, ContainerResponse containerResponse) {
        if (containerRequest.isSecure()) {
            MultivaluedMap<String, Object> headers = containerResponse.getHttpHeaders();
            List<Object> hstsValues = new ArrayList<>();
            hstsValues.add(String.format("%s=%s", MAX_AGE, maxAge));
            if (includeSubDomains) {
                hstsValues.add(INCLUDE_SUB_DOMAINS);
            }
            headers.put(HSTS, hstsValues);
        }
        return containerResponse;
    }
}

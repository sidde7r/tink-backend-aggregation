package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.filter;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkStorage;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class WizinkFirstFullRefreshErrorFilter extends Filter {

    private WizinkStorage wizinkStorage;

    public WizinkFirstFullRefreshErrorFilter(WizinkStorage wizinkStorage) {
        this.wizinkStorage = wizinkStorage;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        final HttpResponse response = nextFilter(httpRequest);
        if (response.getStatus() >= 400 && wizinkStorage.getFirstFullRefreshFlag()) {
            log.info(
                    "Unable to fetch accounts during first refresh. Set {} flag to false.",
                    StorageKeys.FIRST_FULL_REFRESH);
            wizinkStorage.markIsNotFirstFullRefresh();
        }
        return response;
    }
}

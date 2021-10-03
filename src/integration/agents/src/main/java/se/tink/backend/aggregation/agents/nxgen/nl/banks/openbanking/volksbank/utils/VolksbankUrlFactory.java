package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.utils;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public final class VolksbankUrlFactory {

    private final String bankPath;

    public URL buildURL(final String host, final String uri) {
        return new URL(host + VolksbankConstants.Urls.BASE_PATH + bankPath + uri);
    }
}

package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank;

import se.tink.backend.aggregation.nxgen.http.URL;

public final class VolksbankUrlFactory {

    private final String bankPath;

    VolksbankUrlFactory(final String bankPath) {
        this.bankPath = bankPath;
    }

    public URL buildURL(String uri) {

        StringBuilder s = new StringBuilder();
        s.append(VolksbankConstants.Urls.HOST);
        s.append(VolksbankConstants.Urls.BASE_PATH);
        s.append(bankPath);
        s.append(VolksbankConstants.Urls.SANDBOX_PATH);
        s.append(uri);
        return new URL(s.toString());
    }
}

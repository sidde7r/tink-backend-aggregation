package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank;

import se.tink.backend.aggregation.nxgen.http.URL;

public final class VolksbankUrlFactory {

    private final String bankPath;
    private final boolean isSandbox;

    VolksbankUrlFactory(final String bankPath, final boolean isSandbox) {
        this.bankPath = bankPath;
        this.isSandbox = isSandbox;
    }

    public URL buildURL(final String uri) {

        StringBuilder s = new StringBuilder();
        s.append(VolksbankConstants.Urls.HOST);
        s.append(VolksbankConstants.Urls.BASE_PATH);
        s.append(bankPath);

        if (isSandbox) {
            s.append(VolksbankConstants.Urls.SANDBOX_PATH);
        }

        s.append(uri);
        return new URL(s.toString());
    }
}

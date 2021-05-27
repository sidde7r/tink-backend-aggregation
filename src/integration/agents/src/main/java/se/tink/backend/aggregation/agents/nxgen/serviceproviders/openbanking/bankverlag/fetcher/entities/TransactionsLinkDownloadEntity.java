package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.fetcher.entities;

import lombok.Getter;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Getter
public class TransactionsLinkDownloadEntity {
    private URL href;
}

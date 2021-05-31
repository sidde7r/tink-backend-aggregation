package se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TransactionsLinksEntity {
    private TransactionsLinkDownloadEntity download;
}

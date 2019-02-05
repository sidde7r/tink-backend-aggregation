package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Objects;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaginationEntity {
    @JsonProperty("nextPage")
    private NextPage nextPage;

    @JsonProperty("self")
    private Self self;

    public NextPage getNextPage() {
        return nextPage;
    }

    public Self getSelf() {
        return self;
    }

    public boolean hasNextPage() {
        return Objects.nonNull(nextPage) && !Strings.isNullOrEmpty(nextPage.href);
    }

    public static class NextPage {

        @JsonProperty("href")
        private String href;

        public String getHref() {
            return href;
        }
    }

    public static class Self {

        @JsonProperty("href")
        private String href;

        public String getHref() {
            return href;
        }
    }
}

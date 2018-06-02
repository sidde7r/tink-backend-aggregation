package se.tink.backend.rpc;

import io.swagger.annotations.ApiModelProperty;

import io.protostuff.Exclude;
import io.protostuff.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.tink.backend.core.SearchQuery;
import se.tink.backend.core.SearchResult;
import se.tink.backend.core.StringDoublePair;

public class SearchResponse {
    @Tag(1)
    @ApiModelProperty(name = "count", value="Number of results returned.", example = "110", required = true)
    private int count;
    @Exclude
    private Map<SearchResponseMetricTypes, Object> metrics; // For backwards compatibility
    @Tag(2)
    @ApiModelProperty(name = "periodAmounts", value="Key value object holding periods and statistics values for result with the period specified in query.", required = true)
    private List<StringDoublePair> periodAmounts;
    @Tag(3)
    @ApiModelProperty(name = "query", value="The query executed.", required = true)
    private SearchQuery query;
    @Tag(4)
    @ApiModelProperty(name = "results", value="The search result.", required = true)
    private List<SearchResult> results;
    @Tag(5)
    @ApiModelProperty(name = "net", value="The transaction amount net of the result.", example = "1288.45", required = true)
    private double net;

    public int getCount() {
        return count;
    }

    @ApiModelProperty(name = "metrics", hidden = true)
    public Map<SearchResponseMetricTypes, Object> getMetrics() {
        return metrics;
    }

    public SearchQuery getQuery() {
        return query;
    }

    public List<SearchResult> getResults() {
        return results;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setMetric(SearchResponseMetricTypes type, Object metric) {
        if (metrics == null) {
            metrics = new HashMap<SearchResponseMetricTypes, Object>();
        }

        metrics.put(type, metric);
    }

    public void setMetrics(Map<SearchResponseMetricTypes, Object> metrics) {
        this.metrics = metrics;
    }

    public void setQuery(SearchQuery query) {
        this.query = query;
    }

    public void setResults(List<SearchResult> results) {
        this.results = results;
    }

    public List<StringDoublePair> getPeriodAmounts() {
        return periodAmounts;
    }

    public void setPeriodAmounts(List<StringDoublePair> periodAmounts) {
        this.periodAmounts = periodAmounts;
    }

    public double getNet() {
        return net;
    }

    public void setNet(double net) {
        this.net = net;
    }
}

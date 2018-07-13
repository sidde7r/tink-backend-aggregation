package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AbstractLinkResponse {
    @JsonProperty("_links")
    protected HashMap<String, LinkEntity> linksMap;
    protected List<LinkEntity> links;

    public HashMap<String, LinkEntity> getLinksMap() {
        return linksMap != null ? linksMap : Maps.newHashMap();
    }

    public void setLinksMap(HashMap<String, LinkEntity> linksMap) {
        this.linksMap = linksMap;
    }

    public List<LinkEntity> getLinks() {
        return links != null ? links : Lists.newArrayList();
    }

    public void setLinks(List<LinkEntity> links) {
        this.links = links;
    }
}

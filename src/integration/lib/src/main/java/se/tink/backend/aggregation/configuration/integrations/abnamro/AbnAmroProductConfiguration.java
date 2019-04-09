package se.tink.backend.aggregation.configuration.integrations.abnamro;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

public class AbnAmroProductConfiguration {

    public AbnAmroProductConfiguration() {}

    AbnAmroProductConfiguration(ImmutableList<Integer> ids, ImmutableList<String> groups) {
        this.ids = ids;
        this.groups = groups;
    }

    @JsonProperty private ImmutableList<Integer> ids;
    @JsonProperty private ImmutableList<String> groups;

    public ImmutableList<String> getGroups() {
        return groups;
    }

    public void setGroups(ImmutableList<String> groups) {
        this.groups = groups;
    }

    public ImmutableList<Integer> getIds() {
        return ids;
    }

    public void setIds(ImmutableList<Integer> ids) {
        this.ids = ids;
    }
}

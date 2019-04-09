package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimelineItemGroupEntity {
    private String date;
    private List<TimelineItemEntity> subItems;

    public List<TimelineItemEntity> getSubItems() {
        return subItems;
    }

    public void setSubItems(List<TimelineItemEntity> subItems) {
        this.subItems = subItems;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

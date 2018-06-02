package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DetailsEntity {
    @JsonProperty("Id")
    private String id;
    @JsonProperty("Label")
    private String label;
    @JsonProperty("RowNo")
    private String rowNumber;
    @JsonProperty("SectionId")
    private String sectionId;
    @JsonProperty("Value")
    private String value;
    @JsonProperty("ValueContent")
    private String valueContent;

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getRowNumber() {
        return rowNumber;
    }

    public String getSectionId() {
        return sectionId;
    }

    public String getValue() {
        return value;
    }

    public String getValueContent() {
        return valueContent;
    }
}

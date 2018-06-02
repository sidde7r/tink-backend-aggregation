package se.tink.backend.rpc;

import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import java.util.Set;

public class ActivityQuery {
    @ApiModelProperty(name = "offset", value = "The number of activities to skip (when paging).", required = false, example = "0")
    private int offset;
    @ApiModelProperty(name = "limit", value = "The maximum number of activities to return (when paging, 0 indicates no limit).", required = false, example = "10")
    private int limit;
    @ApiModelProperty(name = "types", value = "The set of activity types to be used as a query filter", required = false, example = "[\"unusual-category-high\", \"unusual-category-low\"]")
    private Set<String> types;
    @ApiModelProperty(name = "startDate", value = "The start date to be used as a query filter", example = "1455740874875")
    private Date startDate;
    @ApiModelProperty(name = "endDate", value = "The end date to be used as a query filter", example = "1455740874875")
    private Date endDate;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Set<String> getTypes() {
        return types;
    }

    public void setTypes(Set<String> types) {
        this.types = types;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}

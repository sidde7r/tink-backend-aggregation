package se.tink.backend.core;

import io.protostuff.Exclude;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import java.util.List;

public class SearchQuery {
    @Tag(11)
    @ApiModelProperty(name = "accounts", value="The list of account IDs to be used as a query filter", required = false, example = "[\"87fa44ec11c4426e889a963add92b69e\"]")
    private List<String> accounts;
    @Tag(12)
    @ApiModelProperty(name = "categories", value="The list of category IDs to be used as a query filter", required = false, example = "[\"953c4eda24554a61a9653a479e70fc96\"]")
    private List<String> categories;
    @Tag(13)
    @ApiModelProperty(name = "credentials", value="The list of credentials IDs to be used as a query filter", required = false, example = "[\"18bb1f4636894f3bba8ddcd567d22fbd\"]")
    private List<String> credentials;
    @Tag(14)
    @ApiModelProperty(name = "externalIds", value = "A list of external IDs to filter for", example = "[\"953c4eda24554a61a9653a479e70fc96\"]")
    private List<String> externalIds;
    @Tag(9)
    @ApiModelProperty(name = "endDate", value="The end date of the result.", required = false, example = "1455740874875")
    private Date endDate;
    @Tag(1)
    @ApiModelProperty(name = "limit", value="The limit for the result, used for paging.", example = "20", required = false)
    private int limit = 0;
    @Tag(2)
    @ApiModelProperty(name = "location", hidden = true)
    private Location location;
    @Tag(3)
    @ApiModelProperty(name = "locationDistance", hidden = true)
    private double locationDistance;
    @Tag(4)
    @ApiModelProperty(name = "offset", value="The offset for the result, used for paging.", example = "20", required = false)
    private int offset = 0;
    @Tag(5)
    @ApiModelProperty(name = "order", value="The order of the result.", example = "ASC", required = false)
    private OrderTypes order;
    @Tag(6)
    @ApiModelProperty(name = "queryString", value="The string query.", example = "Food this week", required = false)
    private String queryString;
    @Tag(7)
    @ApiModelProperty(name = "sort", value="The sort order of the result.", example = "DATE", required = false)
    private SearchSortTypes sort;
    @Tag(8)    
    @ApiModelProperty(name = "startDate", value="The start date of the result.", required = false, example = "1455740874875")
    private Date startDate;
    @Exclude
    @ApiModelProperty(name = "transactionId", hidden = true)
    private String transactionId;
    @Tag(10)    
    @ApiModelProperty(name = "includeUpcoming", value="Indicates if result should include upcoming transactions.", required = false)
    private boolean includeUpcoming;
    @Exclude
    @ApiModelProperty(name = "lastTransactionId", hidden = true)
    private String lastTransactionId;

    public List<String> getAccounts() {
        return accounts;
    }

    public List<String> getCategories() {
        return categories;
    }

    public List<String> getCredentials() {
        return credentials;
    }

    public List<String> getExternalIds() {
        return externalIds;
    }

    public Date getEndDate() {
        return endDate;
    }

    public int getLimit() {
        return limit;
    }

    public Location getLocation() {
        return location;
    }

    public double getLocationDistance() {
        return locationDistance;
    }

    public int getOffset() {
        return offset;
    }

    public OrderTypes getOrder() {
        return order;
    }

    public String getQueryString() {
        return queryString;
    }

    public SearchSortTypes getSort() {
        return sort;
    }

    public Date getStartDate() {
        return startDate;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setAccounts(List<String> accounts) {
        this.accounts = accounts;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public void setCredentials(List<String> credentials) {
        this.credentials = credentials;
    }

    public void setExternalIds(List<String> externalIds) {
        this.externalIds = externalIds;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setLocationDistance(double locationDistance) {
        this.locationDistance = locationDistance;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setOrder(OrderTypes order) {
        this.order = order;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public void setSort(SearchSortTypes sort) {
        this.sort = sort;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public boolean isIncludeUpcoming() {
        return includeUpcoming;
    }

    public void setIncludeUpcoming(boolean includeUpcoming) {
        this.includeUpcoming = includeUpcoming;
    }

    public String getLastTransactionId() {
        return lastTransactionId;
    }

    public void setLastTransactionId(String lastTransactionId) {
        this.lastTransactionId = lastTransactionId;
    }
}

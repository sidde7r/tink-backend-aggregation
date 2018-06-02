package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.protostuff.Tag;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import se.tink.libraries.date.ResolutionTypes;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionQuery {
    @Tag(1)
    private List<String> accounts;
    @Tag(2)
    private List<String> categories;
    @Tag(3)
    private List<String> credentials;
    @Tag(4)
    private String id;
    @Tag(5)
    private int limit = 0;
    @Tag(6)
    private int offset = 0;
    @Tag(7)
    private OrderTypes order;
    @Tag(8)
    private List<String> periods;
    @Tag(9)
    private ResolutionTypes resolution;
    @Tag(10)
    private TransactionSortTypes sort;
    @Tag(11)
    private List<String> types;
    @Tag(12)
    private Date endDate;
    @Tag(13)
    private boolean includeUpcoming;
    @Tag(14)
    private List<String> tags;

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getAccounts() {
        return accounts;
    }

    public List<String> getCategories() {
        return categories;
    }

    public List<String> getCredentials() {
        return credentials;
    }

    public String getId() {
        return id;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public OrderTypes getOrder() {
        return order;
    }

    public List<String> getPeriods() {
        return periods;
    }

    public ResolutionTypes getResolution() {
        return resolution;
    }

    public TransactionSortTypes getSort() {
        return sort;
    }

    public List<String> getTypes() {
        return types;
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

    public void setId(String id) {
        this.id = id;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setOrder(OrderTypes order) {
        this.order = order;
    }

    public void setPeriods(List<String> periods) {
        this.periods = periods;
    }

    public void setResolution(ResolutionTypes resolution) {
        this.resolution = resolution;
    }

    public void setSort(TransactionSortTypes sort) {
        this.sort = sort;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isIncludeUpcoming() {
        return includeUpcoming;
    }

    public void setIncludeUpcoming(boolean includeUpcoming) {
        this.includeUpcoming = includeUpcoming;
    }
}

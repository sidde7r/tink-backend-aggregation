package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import se.tink.backend.utils.StringUtils;

@SuppressWarnings("serial")
@Entity
@Table(name = "categories")
public class Category implements Serializable {

    public Category() {
        id = StringUtils.generateUUID();
    }

    public Category(String primaryName, String secondaryName, String code, int sortOrder, CategoryTypes type) {
        this(primaryName, secondaryName, code, sortOrder, false, type);
    }

    public Category(String primaryName, String secondaryName, String code, int sortOrder, boolean defaultChild, CategoryTypes type) {
        this();

        this.primaryName = primaryName;
        this.secondaryName = secondaryName;
        this.code = code;
        this.sortOrder = sortOrder;
        this.defaultChild = defaultChild;
        this.type = type;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("type", type).add("primaryName", primaryName)
                .add("secondaryName", secondaryName).toString();
    }

    @Tag(1)
    @ApiModelProperty(name = "code", value = "Machine readable category code.", example = "expenses:food.restaurants", required = true)
    protected String code;
    @Tag(2)
    @ApiModelProperty(name = "defaultChild", value = "Indicates if this is the default child to be used when categorizing to a primary level category.", example = "false", required = true)
    protected boolean defaultChild;
    @Id
    @Tag(3)
    @ApiModelProperty(name = "id", value = "The internal identifier of the category, referenced by e.g. a transaction.", example = "7e88d58188ee49749adca59e152324b6", required = true)
    protected String id;
    @Tag(4)
    @ApiModelProperty(name = "parent", value = "The parent internal identifier of this category, or null.", example = "067fa4c769774ae980435c76be328c0b")
    protected String parent;
    @Transient
    @Tag(5)
    @ApiModelProperty(name = "primaryName", value = "The primary name of this category.", example = "Food & Drinks")
    protected String primaryName;
    @Transient
    @Exclude
    @ApiModelProperty(name = "searchTerms", value = "Used by search engine to find transaction with this category.", example = "food,lunch,snacks")
    protected String searchTerms;
    @Transient
    @Tag(6)
    @ApiModelProperty(name = "secondaryName", value = "The secondary name of this category.", example = "Restaurants")
    protected String secondaryName;
    @Tag(7)
    @ApiModelProperty(name = "sortOrder", value = "Sort order for nicer display for the user.", example = "45", required = true)
    protected int sortOrder;
    @Enumerated(EnumType.STRING)
    @Tag(8)
    @ApiModelProperty(name = "type", value = "Type of the category.", example = "EXPENSES", required = true)
    protected CategoryTypes type;
    @Transient
    @Tag(9)
    @ApiModelProperty(name = "typeName", value = "Type name of the category.", example = "Expenses", required = true)
    protected String typeName;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getCode() {
        return code;
    }

    public String getId() {
        return this.id;
    }

    public String getParent() {
        return parent;
    }

    public String getPrimaryName() {
        return this.primaryName;
    }

    public String getSecondaryName() {
        return this.secondaryName;
    }

    public CategoryTypes getType() {
        return type;
    }

    public boolean isDefaultChild() {
        return defaultChild;
    }

    public void setDefaultChild(boolean defaultChild) {
        this.defaultChild = defaultChild;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public void setPrimaryName(String primaryName) {
        this.primaryName = primaryName;
    }

    public void setSecondaryName(String secondaryName) {
        this.secondaryName = secondaryName;
    }

    public void setType(CategoryTypes type) {
        this.type = type;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getSearchTerms() {
        return searchTerms;
    }

    public void setSearchTerms(String searchTerms) {
        this.searchTerms = searchTerms;
    }

    @JsonIgnore
    public String getDisplayName() {
        return (!isDefaultChild() && getSecondaryName() != null) ? getSecondaryName() : getPrimaryName();
    }

    @JsonIgnore
    public Iterable<Category> getDirectChildCategories(Iterable<Category> categories) {
        return getDirectChildCategories(getId(), categories);
    }

    @JsonIgnore
    public Category getDefaultChild(Iterable<Category> categories) {
        return Iterables.find(getDirectChildCategories(categories), Category::isDefaultChild, null);
    }

    public static List<Category> getAllChildCategories(String categoryId, List<Category> categories) {
        ArrayList<Category> result = new ArrayList<Category>();

        for (Category c : categories) {
            if (c.getParent() != null && c.getParent().equals(categoryId)) {
                result.add(c);
                result.addAll(getAllChildCategories(c.getId(), categories));
            }
        }

        return result;
    }

    public static Iterable<Category> getDirectChildCategories(String categoryId, Iterable<Category> categories) {
        Collection<Category> result = new ArrayList<Category>();

        for (Category c : categories) {
            if (c.parent != null && c.parent.equals(categoryId)) {
                result.add(c);
            }
        }

        return result;
    }

    public static List<String> getCategoryIds(List<Category> categories) {
        ArrayList<String> result = new ArrayList<String>();

        for (Category c : categories) {
            result.add(c.getId());
        }

        return result;
    }
}

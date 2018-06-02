package se.tink.backend.core;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "categories_translations")
public class CategoryTranslation {
    protected String code;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected long id;
    protected String locale;
    protected String primaryName;
    protected String searchTerms;
    protected String secondaryName;
    protected String typeName;

    public String getCode() {
        return code;
    }

    public long getId() {
        return id;
    }

    public String getLocale() {
        return locale;
    }

    public String getPrimaryName() {
        return primaryName;
    }

    public String getSearchTerms() {
        return searchTerms;
    }

    public String getSecondaryName() {
        return secondaryName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setPrimaryName(String primaryName) {
        this.primaryName = primaryName;
    }

    public void setSearchTerms(String searchTerms) {
        this.searchTerms = searchTerms;
    }

    public void setSecondaryName(String secondaryName) {
        this.secondaryName = secondaryName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}

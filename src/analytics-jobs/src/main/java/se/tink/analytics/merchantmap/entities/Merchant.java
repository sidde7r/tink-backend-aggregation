package se.tink.analytics.merchantmap.entities;

import java.io.Serializable;

/**
 * Simple copy of the merchant entity with the interesting fields for this domain
 */
public class Merchant implements Serializable {

    private static final long serialVersionUID = 6013326125503446129L;

    private String name;
    private String source;
    private String parentId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}

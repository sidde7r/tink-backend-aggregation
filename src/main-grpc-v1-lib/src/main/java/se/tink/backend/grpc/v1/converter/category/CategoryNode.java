package se.tink.backend.grpc.v1.converter.category;

import com.google.common.collect.Maps;
import java.util.Map;

public class CategoryNode {
    private String code;
    private String id;
    private String name;
    private int sortOrder;
    private boolean defaultChild;
    private Map<String, CategoryNode> children = Maps.newHashMap();
    private String[] codesByLevel;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
        this.codesByLevel = code.split(":|\\."); // `:` or `.`
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isDefaultChild() {
        return defaultChild;
    }

    public void setDefaultChild(boolean defaultChild) {
        this.defaultChild = defaultChild;
    }

    public Map<String, CategoryNode> getChildren() {
        return children;
    }

    public void setChildren(Map<String, CategoryNode> children) {
        this.children = children;
    }

    public void putChild(String code, CategoryNode child) {
        this.children.put(code, child);
    }

    public String getCodeByLevel(int level) {
        if (level < 0 || level >= codesByLevel.length) {
            return null;
        }
        return codesByLevel[level];
    }

    public int getLevel() {
        return codesByLevel.length - 1;
    }
}

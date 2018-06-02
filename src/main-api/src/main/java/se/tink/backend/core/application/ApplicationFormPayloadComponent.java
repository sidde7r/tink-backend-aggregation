package se.tink.backend.core.application;

import java.util.List;

public class ApplicationFormPayloadComponent {

    private String type;
    private String title;
    private String description;
    private String name;
    private List<String> list;
    private String url;
    private List<ProviderComparison> comparisons;
    private List<List<String>> table; // This is a list of rows that includes a list with columns.
    private String color;
    private List<ConfirmationFormListData> confirmationFormListData;
    private String serializedPayload;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<ProviderComparison> getComparisons() {
        return comparisons;
    }

    public void setComparisons(List<ProviderComparison> comparisons) {
        this.comparisons = comparisons;
    }

    public List<List<String>> getTable() {
        return table;
    }

    public void setTable(List<List<String>> table) {
        this.table = table;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<ConfirmationFormListData> getConfirmationFormListData() {
        return confirmationFormListData;
    }

    public void setConfirmationFormListData(
            List<ConfirmationFormListData> confirmationFormListData) {
        this.confirmationFormListData = confirmationFormListData;
    }
    
    public void setSerializedPayload(String serializedPayload) {
        this.serializedPayload = serializedPayload;
    }
    
    public String getSerializedPayload() {
        return this.serializedPayload;
    }
}

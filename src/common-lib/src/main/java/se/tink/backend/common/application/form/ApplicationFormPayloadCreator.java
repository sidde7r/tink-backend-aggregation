package se.tink.backend.common.application.form;

import java.util.List;
import java.util.Map;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.core.application.ApplicationFormPayloadComponent;
import se.tink.backend.core.application.ConfirmationFormListData;
import se.tink.backend.core.application.ProviderComparison;
import se.tink.backend.core.enums.ApplicationComponentType;

public class ApplicationFormPayloadCreator {

    public ApplicationFormPayloadCreator() {}

    public ApplicationFormPayloadComponent createComponentHeader(String title, String url, String color) {
        ApplicationFormPayloadComponent component = new ApplicationFormPayloadComponent();
        component.setType(ApplicationComponentType.HEADER);
        component.setTitle(title);
        component.setUrl(url);
        component.setColor(color);
        return component;
    }

    public ApplicationFormPayloadComponent createComponentHeader(String title, String description, String url,
            String color) {
        ApplicationFormPayloadComponent component = createComponentHeader(title, url, color);
        component.setDescription(description);
        return component;
    }

    public ApplicationFormPayloadComponent createComponentTextBlock(String name, String title, String description,
            Map<String, Object> payload) {
        
        ApplicationFormPayloadComponent component = createComponentTextBlock(name, title, description);
        component.setSerializedPayload(SerializationUtils.serializeToString(payload));
        return component;
    }
    
    public ApplicationFormPayloadComponent createComponentTextBlock(String name, String title, String description) {
        ApplicationFormPayloadComponent component = createComponentTextBlock(title, description);
        component.setName(name);
        return component;
    }
    
    public ApplicationFormPayloadComponent createComponentTextBlock(String title, String description) {
        ApplicationFormPayloadComponent component = new ApplicationFormPayloadComponent();
        component.setType(ApplicationComponentType.TEXT_BLOCK);
        component.setTitle(title);
        component.setDescription(description);
        return component;
    }
    
    public ApplicationFormPayloadComponent createComponentTextBlock(String description) {
        return createComponentTextBlock(null, description);
    }

    public ApplicationFormPayloadComponent createComponentComparision(String title, List<ProviderComparison> comparisons) {
        ApplicationFormPayloadComponent component = new ApplicationFormPayloadComponent();
        component.setType(ApplicationComponentType.COMPARISON);
        component.setTitle(title);
        component.setComparisons(comparisons);
        return component;
    }
    
    public ApplicationFormPayloadComponent createComponentComparision(List<ProviderComparison> comparisons) {
        return createComponentComparision(null, comparisons);
    }

    public ApplicationFormPayloadComponent createComponentList(String title, List<String> list, String listName) {
        return createComponentList(ApplicationComponentType.LIST, title, list, listName);
    }

    private ApplicationFormPayloadComponent createComponentList(String listType, String title, List<String> list,
            String listName) {
        ApplicationFormPayloadComponent component = new ApplicationFormPayloadComponent();
        component.setType(listType);
        component.setTitle(title);
        component.setName(listName);
        component.setList(list);
        return component;
    }

    public ApplicationFormPayloadComponent createComponentLink(String description, String url) {
        return createComponentLink(null, description, url, null);
    }

    public ApplicationFormPayloadComponent createComponentLink(String description, String url, List<String> details) {
        return createComponentLink(null, description, url, details);
    }

    public ApplicationFormPayloadComponent createComponentLink(String name, String description, String url) {
        return createComponentLink(name, description, url, null);
    }

    public ApplicationFormPayloadComponent createComponentLink(String name, String description, String url,
            List<String> details) {
        
        ApplicationFormPayloadComponent component = new ApplicationFormPayloadComponent();
        component.setType(ApplicationComponentType.LINK);
        component.setName(name);
        component.setDescription(description);
        component.setUrl(url);
        component.setList(details);
        return component;
    }

    public ApplicationFormPayloadComponent createComponentLinkForApplicationSummary(String name, String description, String url, List<ConfirmationFormListData> details) {
        ApplicationFormPayloadComponent component = new ApplicationFormPayloadComponent();
        component.setType(ApplicationComponentType.LINK);
        component.setDescription(description);
        component.setName(name);
        component.setUrl(url);
        component.setConfirmationFormListData(details);
        return component;
    }

    /*
     * `table` is a list of row vectors (i.e. outer list represent rows, and inner list represent columns).
     */
    public ApplicationFormPayloadComponent createComponentTable(String title, List<List<String>> table) {
        ApplicationFormPayloadComponent component = new ApplicationFormPayloadComponent();
        component.setType(ApplicationComponentType.TABLE);
        component.setTitle(title);
        component.setTable(table);
        return component;
    }

    public ApplicationFormPayloadComponent createComponentTable(String title, List<List<String>> table, String name) {
        ApplicationFormPayloadComponent component = createComponentTable(title, table);
        component.setName(name);
        return component;
    }
    
    public ApplicationFormPayloadComponent createComponentTable(List<List<String>> table, String name) {
        ApplicationFormPayloadComponent component = createComponentTable(null, table);
        component.setName(name);
        return component;
    }

}

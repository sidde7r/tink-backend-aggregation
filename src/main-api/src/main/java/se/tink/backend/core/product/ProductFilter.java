package se.tink.backend.core.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ProductFilter {

    public static final TypeReference<List<ProductFilterRule>> LIST_OF_RULES = new TypeReference<List<ProductFilterRule>>() {
    };

    private UUID id;
    private String rulesSerialized;
    private String status;
    private UUID templateId;
    private String version;

    public ProductFilter() {
        id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public List<ProductFilterRule> getRules() {
        if (Strings.isNullOrEmpty(rulesSerialized)) {
            return Lists.newArrayList();
        }

        return SerializationUtils.deserializeFromString(rulesSerialized, LIST_OF_RULES);
    }

    public String getRulesSerialized() {
        return rulesSerialized;
    }

    public ProductFilterStatus getStatus() {
        return ProductFilterStatus.valueOf(status);
    }

    public UUID getTemplateId() {
        return templateId;
    }

    public String getVersion() {
        return version;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setRules(List<ProductFilterRule> rules) {
        setRulesSerialized(SerializationUtils.serializeToString(rules));
    }

    public void setRulesSerialized(String rulesSerialized) {
        this.rulesSerialized = rulesSerialized;
    }

    public void setStatus(ProductFilterStatus status) {
        if (status == null) {
            this.status = null;
        } else {
            this.status = status.name();
        }
    }

    public void setTemplateId(UUID templateId) {
        this.templateId = templateId;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}

package se.tink.backend.core.product;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;
import se.tink.backend.serialization.TypeReferences;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ProductTemplate {
    private UUID id;
    private String name;
    private String propertiesSerialized;
    private String providerName;
    private String status;
    private String type;

    public ProductTemplate() {
        id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getProperties() {
        if (Strings.isNullOrEmpty(propertiesSerialized)) {
            return Maps.newHashMap();
        }

        return SerializationUtils.deserializeFromString(propertiesSerialized, TypeReferences.MAP_OF_STRING_OBJECT);
    }

    public String getPropertiesSerialized() {
        return propertiesSerialized;
    }

    public Object getProperty(String key) {

        Map<String, Object> properties = getProperties();

        if (properties == null) {
            return null;
        }

        return properties.get(key);
    }

    public String getProviderName() {
        return providerName;
    }

    public ProductTemplateStatus getStatus() {
        return ProductTemplateStatus.valueOf(status);
    }

    public ProductType getType() {
        return ProductType.valueOf(type);
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProperties(Map<String, Object> properties) {
        setPropertiesSerialized(SerializationUtils.serializeToString(properties));
    }

    public void setPropertiesSerialized(String propertiesSerialized) {
        this.propertiesSerialized = propertiesSerialized;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public void setStatus(ProductTemplateStatus status) {
        if (status == null) {
            this.status = null;
        } else {
            this.status = status.name();
        }
    }

    public void setType(ProductType type) {
        if (type == null) {
            this.type = null;
        } else {
            this.type = type.name();
        }
    }
}

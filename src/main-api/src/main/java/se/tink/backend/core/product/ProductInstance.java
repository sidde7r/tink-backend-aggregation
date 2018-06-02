package se.tink.backend.core.product;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.serialization.TypeReferences;

@Table(value = "products_instances")
public class ProductInstance {
    private UUID filterId;
    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private UUID id;
    @Column(value = "properties")
    private String propertiesSerialized;
    private UUID templateId;
    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;
    private Date validFrom;
    private Date validTo;
    
    public ProductInstance() {
        id = UUID.randomUUID();
    }

    public UUID getFilterId() {
        return filterId;
    }

    public UUID getId() {
        return id;
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

    public UUID getTemplateId() {
        return templateId;
    }

    public UUID getUserId() {
        return userId;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }

    public void setFilterId(UUID filterId) {
        this.filterId = filterId;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setProperties(Map<String, Object> properties) {
        setPropertiesSerialized(SerializationUtils.serializeToString(properties));
    }

    public void setPropertiesSerialized(String propertiesSerialized) {
        this.propertiesSerialized = propertiesSerialized;
    }

    public void setTemplateId(UUID templateId) {
        this.templateId = templateId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }
}

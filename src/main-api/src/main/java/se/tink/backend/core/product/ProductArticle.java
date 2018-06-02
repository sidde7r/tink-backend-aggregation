package se.tink.backend.core.product;

import com.google.common.base.Preconditions;
import java.util.Date;
import java.util.UUID;

public class ProductArticle {
    
    private final ProductInstance instance;
    private final ProductTemplate template;

    public ProductArticle(ProductTemplate template, ProductInstance instance) {
        Preconditions.checkNotNull(template);
        Preconditions.checkNotNull(instance);
        
        this.template = template;
        this.instance = instance;
    }
    
    public UUID getFilterId() {
        return instance.getFilterId();
    }

    public UUID getInstanceId() {
        return instance.getId();
    }
    
    public String getName() {
        return template.getName();
    }
    
    public Object getProperty(ProductPropertyKey key) {
        if (key == null) {
            return null;
        }
        
        return getProperty(key.getKey());
    }

    public Object getProperty(String key) {
        
        Object property;
        
        property = instance.getProperty(key);
        if (property != null) {
            return property;
        }
        
        property = template.getProperty(key);
        if (property != null) {
            return property;
        }
        
        return null;
    }

    public String getProviderName() {
        return template.getProviderName();
    }
    
    public ProductTemplateStatus getStatus() {
        return template.getStatus();
    }

    public UUID getTemplateId() {
        return template.getId();
    }

    public ProductType getType() {
        return template.getType();
    }

    public UUID getUserId() {
        return instance.getUserId();
    }

    public Date getValidFrom() {
        return instance.getValidFrom();
    }

    public Date getValidTo() {
        return instance.getValidTo();
    }
    
    public boolean hasProperty(ProductPropertyKey key) {
        return getProperty(key) != null;
    }
}

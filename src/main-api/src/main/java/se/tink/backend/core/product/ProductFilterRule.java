package se.tink.backend.core.product;


public class ProductFilterRule {
    private String type;
    private Object payload;
    
    public ProductFilterRule() {
        
    }

    public ProductFilterRule(ProductFilterRuleType type, Object payload) {
        setType(type);
        setPayload(payload);
    }
    
    public ProductFilterRuleType getType() {
        return ProductFilterRuleType.valueOf(type);
    }
    
    public Object getPayload() {
        return payload;
    }
    
    public void setType(ProductFilterRuleType type) {
        if (type == null) {
            this.type = null;
        } else {
            this.type = type.name();
        }
    }
    
    public void setPayload(Object payload) {
        this.payload = payload;
    }
}

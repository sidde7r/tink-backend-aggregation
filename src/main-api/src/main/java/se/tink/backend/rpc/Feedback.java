package se.tink.backend.rpc;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "feedback")
public class Feedback {
    public enum ObjectTypes {
        ACTIVITY
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected long id;
    protected long inserted;
    
    protected String objectId;
  
    @Enumerated(EnumType.STRING)
    protected ObjectTypes objectType;
    
    protected String opinion;

    @Type(type = "text")
    protected String payload;
    

    public Feedback() {
        inserted = (new Date()).getTime();
    }
    
    public long getId() {
        return id;
    }
    
    public long getInserted() {
        return inserted;
    }
    
    public String getObjectId() {
        return objectId;
    }
    
    public ObjectTypes getObjectType() {
        return objectType;
    }
    
    public String getOpinion() {
        return opinion;
    }
    
    public String getPayload() {
        return payload;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public void setInserted(long inserted) {
        this.inserted = inserted;
    }
    
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
    
    public void setObjectType(ObjectTypes objectType) {
        this.objectType = objectType;
    }
    
    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }
    
    public void setPayload(String payload) {
        this.payload = payload;
    }
}

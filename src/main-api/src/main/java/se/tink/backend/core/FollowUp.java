/**
 * 
 */
package se.tink.backend.core;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import se.tink.backend.utils.StringUtils;

/**
 * Object that holds search that needs to be followed up later 
 */
@Entity
@Table(name = "follow_ups")
public class FollowUp {

    @Id
    private String id;
    private String description;
    
    public String getId() {
        return id;
    }

    public FollowUp() {
        this.id = StringUtils.generateUUID();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

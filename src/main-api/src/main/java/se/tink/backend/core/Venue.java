package se.tink.backend.core;

import se.tink.backend.utils.StringUtils;

public class Venue {
    protected String id;
    protected String name;
    protected Location location;
    
    public Venue() {
        id = StringUtils.generateUUID();
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
}
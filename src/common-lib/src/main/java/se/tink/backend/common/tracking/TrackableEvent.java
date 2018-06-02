package se.tink.backend.common.tracking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import se.tink.backend.core.Event;
import se.tink.backend.utils.LogUtils;

public class TrackableEvent {
    private static final LogUtils log = new LogUtils(TrackableEvent.class);
    private String eventType;
    private String userId;
    private Date date;
    private Map<String, Object> properties;
    private Set<Class<? extends EventTracker>> includedTrackers = Sets.newHashSet(); // Empty = all
    private Set<Class<? extends EventTracker>> excludedTrackers = Sets.newHashSet(); // Empty = none

    public TrackableEvent(String userId) {
        this.userId = userId;
        this.date = new Date();
    }

    public static TrackableEvent userProperties(String userId, Map<String, Object> properties) {
        TrackableEvent e = new TrackableEvent(userId);
        e.setProperties(Maps.newHashMap(properties));
        return e;
    }

    public static TrackableEvent event(String userId, String eventType, Map<String, Object> properties) {
        TrackableEvent e = new TrackableEvent(userId);
        if (properties != null) {
            e.setProperties(Maps.newHashMap(properties));
        }
        e.setEventType(eventType);
        return e;
    }

    public void addProperty(String key, Object value) {
        if (properties == null) {
            properties = Maps.newHashMap();
        }
        properties.put(key, value);
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Date getDate() {
        return date;
    }

    /**
     * By default, all trackers are included.
     * By explicitly including trackers (by calling this method), only those trackers will be included.
     * @param tracker
     */
    public void includeTracker(Class<? extends EventTracker> tracker) {
        if (excludedTrackers.contains(tracker)) {
            excludedTrackers.remove(tracker);
        }
        
        includedTrackers.add(tracker);
    }
    
    /**
     * By default, no trackers are excluded.
     * By explicitly excluding trackers (by calling this method), those trackers will not be included.
     * @param tracker
     */
    public void excludeTracker(Class<? extends EventTracker> tracker) {
        if (includedTrackers.contains(tracker)) {
            includedTrackers.remove(tracker);
        }

        excludedTrackers.add(tracker);
    }
    
    public boolean isTrackerIncluded(Class<? extends EventTracker> tracker) {
        
        // Explicitly excluded.
        if (excludedTrackers.contains(tracker)) {
            return false;
        }
        
        // Implicitly included.
        if (includedTrackers.isEmpty()) {
            return true;
        }
        
        // Explicitly included.
        if (includedTrackers.contains(tracker)) {
            return true;
        }
        
        // Implicitly excluded.
        return false;
    }

    public Event toStorableEvent() {
        try {
            return new Event(userId, date, eventType, properties);
        } catch (JsonProcessingException e) {
            log.error(userId, "Could not serialize properties when storing event", e);
            return null;
        }
    }
}

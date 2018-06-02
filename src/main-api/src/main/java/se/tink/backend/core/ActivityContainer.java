package se.tink.backend.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import org.xerial.snappy.Snappy;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Entity
@Table(name = "activities")
public class ActivityContainer {
    @SuppressWarnings("serial")
    private static class ActivityList extends ArrayList<Activity> {
        @SuppressWarnings("unused")
        public ActivityList() {

        }
    }
    
    @SuppressWarnings("serial")
    protected static class HtmlActivityList extends ArrayList<HtmlActivity> {
        public HtmlActivityList() {

        }
    }
    

    public ActivityContainer() {

    }

    public ActivityContainer(byte[] data) {
        this.data = data;
    }

    public ActivityContainer(String userId, List<Activity> activities) {
        this.userId = userId;

        setActivities(activities);
    }
    
    public ActivityContainer(List<HtmlActivity> htmlActivities, String userId)
    {
    	this.userId = userId;
    	setHtmlActivities(htmlActivities);
    }

	@Lob
    @Column(length = 1048576)
    private byte[] data;

    @Id
    private String userId;

    public byte[] getData() {
        return data;
    }

    public String getUserId() {
        return userId;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setActivities(List<Activity> activities) {
        try {
            this.data = Snappy.compress(SerializationUtils.serializeToBinary(activities));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	private void setHtmlActivities(List<HtmlActivity> htmlActivities) {
        try {
            this.data = Snappy.compress(SerializationUtils.serializeToBinary(htmlActivities));
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
    
    public List<Activity> getActivities() {
        if (data == null) {
            return null;
        }

        try {
            return SerializationUtils.deserializeFromBinary(Snappy.uncompress(data), ActivityList.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<HtmlActivity> getHtmlActivities() {
        if (data == null) {
            return null;
        }

        try {
            return SerializationUtils.deserializeFromBinary(Snappy.uncompress(data), HtmlActivityList.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Settings{

	@JsonProperty("services")
	private List<ServicesItem> services;

	@JsonProperty("notifications")
	private Notifications notifications;

	public void setServices(List<ServicesItem> services){
		this.services = services;
	}

	public List<ServicesItem> getServices(){
		return services;
	}

	public void setNotifications(Notifications notifications){
		this.notifications = notifications;
	}

	public Notifications getNotifications(){
		return notifications;
	}

	@Override
 	public String toString(){
		return 
			"Settings{" + 
			"services = '" + services + '\'' + 
			",notifications = '" + notifications + '\'' + 
			"}";
		}
}
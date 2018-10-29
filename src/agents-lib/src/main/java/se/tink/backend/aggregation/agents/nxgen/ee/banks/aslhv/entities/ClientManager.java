package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class ClientManager{

	@JsonProperty("phone")
	private String phone;

	@JsonProperty("name")
	private String name;

	@JsonProperty("email")
	private String email;

	public void setPhone(String phone){
		this.phone = phone;
	}

	public String getPhone(){
		return phone;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setEmail(String email){
		this.email = email;
	}

	public String getEmail(){
		return email;
	}

	@Override
 	public String toString(){
		return 
			"ClientManager{" + 
			"phone = '" + phone + '\'' + 
			",name = '" + name + '\'' + 
			",email = '" + email + '\'' + 
			"}";
		}
}
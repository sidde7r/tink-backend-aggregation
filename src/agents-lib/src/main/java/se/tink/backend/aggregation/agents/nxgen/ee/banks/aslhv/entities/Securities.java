package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Securities{

	@JsonProperty("total")
	private int total;

	public void setTotal(int total){
		this.total = total;
	}

	public int getTotal(){
		return total;
	}

	@Override
 	public String toString(){
		return 
			"Securities{" + 
			"total = '" + total + '\'' + 
			"}";
		}
}
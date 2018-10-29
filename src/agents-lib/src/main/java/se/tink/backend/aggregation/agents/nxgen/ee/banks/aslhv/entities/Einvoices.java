package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Einvoices{

	@JsonProperty("total")
	private int total;

	@JsonProperty("need_action")
	private int needAction;

	public void setTotal(int total){
		this.total = total;
	}

	public int getTotal(){
		return total;
	}

	public void setNeedAction(int needAction){
		this.needAction = needAction;
	}

	public int getNeedAction(){
		return needAction;
	}

	@Override
 	public String toString(){
		return 
			"Einvoices{" + 
			"total = '" + total + '\'' + 
			",need_action = '" + needAction + '\'' + 
			"}";
		}
}
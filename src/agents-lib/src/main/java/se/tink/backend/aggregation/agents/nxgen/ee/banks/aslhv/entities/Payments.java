package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Payments{

	@JsonProperty("target_portfolio_id")
	private List<String> targetPortfolioId;

	@JsonProperty("enabled")
	private boolean enabled;

	public void setTargetPortfolioId(List<String> targetPortfolioId){
		this.targetPortfolioId = targetPortfolioId;
	}

	public List<String> getTargetPortfolioId(){
		return targetPortfolioId;
	}

	public void setEnabled(boolean enabled){
		this.enabled = enabled;
	}

	public boolean isEnabled(){
		return enabled;
	}

	@Override
 	public String toString(){
		return 
			"Payments{" + 
			"target_portfolio_id = '" + targetPortfolioId + '\'' + 
			",enabled = '" + enabled + '\'' + 
			"}";
		}
}
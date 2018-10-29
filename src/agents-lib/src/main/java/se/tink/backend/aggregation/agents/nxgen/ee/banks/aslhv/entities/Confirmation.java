package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Confirmation{

	@JsonProperty("unsigned")
	private Object unsigned;

	@JsonProperty("signed")
	private Object signed;

	public Object getUnsigned(){
		return unsigned;
	}

	public Object getSigned(){
		return signed;
	}
}
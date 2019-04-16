package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionDetails{

	@JsonProperty("sectionHeaders")
	private SectionHeaders sectionHeaders;

	@JsonProperty("billingInfo")
	private BillingInfo billingInfo;

	@JsonProperty("activityList")
	private List<ActivityListItem> activityList;

	@JsonProperty("filterOptions")
	private FilterOptions filterOptions;

	@JsonProperty("status")
	private int status;

	public SectionHeaders getSectionHeaders(){
		return sectionHeaders;
	}

	public BillingInfo getBillingInfo(){
		return billingInfo;
	}

	public List<ActivityListItem> getActivityList(){
		return activityList;
	}

	public FilterOptions getFilterOptions(){
		return filterOptions;
	}

	public int getStatus(){
		return status;
	}
}
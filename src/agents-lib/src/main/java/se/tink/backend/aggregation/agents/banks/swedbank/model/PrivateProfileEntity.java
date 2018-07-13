package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PrivateProfileEntity {
	protected String id;
	protected String bankId;
	private String customerName;
	private String customerNumber;
	private boolean youthProfile;
	private LinksEntity links;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBankId() {
		return bankId;
	}

	public void setBankId(String bankId) {
		this.bankId = bankId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getCustomerNumber() {
		return customerNumber;
	}

	public void setCustomerNumber(String customerNumber) {
		this.customerNumber = customerNumber;
	}

	public boolean isYouthProfile() {
		return youthProfile;
	}

	public void setYouthProfile(boolean youthProfile) {
		this.youthProfile = youthProfile;
	}

	public LinksEntity getLinks() {
		return links;
	}

	public void setLinks(LinksEntity links) {
		this.links = links;
	}
}

package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BankEntity {
	protected String name;
	protected String bankId;
	protected PrivateProfileEntity privateProfile;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBankId() {
		return bankId;
	}

	public void setBankId(String bankId) {
		this.bankId = bankId;
	}

	public PrivateProfileEntity getPrivateProfile() {
		return privateProfile;
	}

	public void setPrivateProfile(PrivateProfileEntity privateProfile) {
		this.privateProfile = privateProfile;
	}

}

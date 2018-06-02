package se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.model.AccountEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {
	protected double totalOwnCapital;
	protected List<AccountEntity> accounts;

	public double getTotalOwnCapital() {
		return totalOwnCapital;
	}

	public void setTotalOwnCapital(double totalOwnCapital) {
		this.totalOwnCapital = totalOwnCapital;
	}

	public List<AccountEntity> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<AccountEntity> accounts) {
		this.accounts = accounts;
	}
}

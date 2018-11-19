package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.EBankingBusinessMessageBulkEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.EbankingValue;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationProcessResponse {
	private EBankingBusinessMessageBulkEntity businessMessageBulk;
	private EbankingValue value;

	public EBankingBusinessMessageBulkEntity getBusinessMessageBulk() {
		return businessMessageBulk;
	}

	public EbankingValue getValue() {
		return value;
	}
}

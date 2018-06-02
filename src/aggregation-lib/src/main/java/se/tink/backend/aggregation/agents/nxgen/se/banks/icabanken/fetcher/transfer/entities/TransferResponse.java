package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc.bankid.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc.bankid.ResponseStatusEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferResponse {
	@JsonProperty("Body")
	private TransferResponseBody body;
	@JsonProperty("ResponseStatus")
	private ResponseStatusEntity responseStatus;

	public TransferResponseBody getBody() {
		return body;
	}

	public ResponseStatusEntity getResponseStatus() {
		return responseStatus;
	}
}

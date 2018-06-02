package se.tink.backend.common.rpc;

public class CreateAggregatorCredentialsRequest {
	public static final String QUEUE_NAME = "createAggregationCredentials";

	protected String credentialsId;
	protected String secretKey;

	public String getCredentialsId() {
		return credentialsId;
	}

	public void setCredentialsId(String credentialsId) {
		this.credentialsId = credentialsId;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
}

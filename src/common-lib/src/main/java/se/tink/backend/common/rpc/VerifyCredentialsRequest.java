package se.tink.backend.common.rpc;

import se.tink.backend.core.Credentials;

public class VerifyCredentialsRequest {
	protected Credentials credentials;

	public Credentials getCredentials() {
		return credentials;
	}

	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}
}

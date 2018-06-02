package se.tink.backend.common.rpc;

public class TrackLocationRequest {
	public static final String QUEUE_NAME = "trackLocation";

	protected String user;

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
}

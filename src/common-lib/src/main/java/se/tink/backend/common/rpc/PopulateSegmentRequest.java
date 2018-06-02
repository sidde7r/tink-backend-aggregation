package se.tink.backend.common.rpc;

public class PopulateSegmentRequest {
	public static final String QUEUE_NAME = "populateSegment";

	protected String segment;

	public String getSegment() {
		return segment;
	}

	public void setSegment(String segment) {
		this.segment = segment;
	}
}

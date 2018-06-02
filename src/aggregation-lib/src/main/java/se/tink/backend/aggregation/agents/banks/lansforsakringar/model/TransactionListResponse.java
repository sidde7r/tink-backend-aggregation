package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionListResponse {
	protected boolean hasMore;
	protected int nextSequenceNumber;

	public boolean getHasMore() {
		return hasMore;
	}

	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
	}

	public int getNextSequenceNumber() {
		return nextSequenceNumber;
	}

	public void setNextSequenceNumber(int nextSequenceNumber) {
		this.nextSequenceNumber = nextSequenceNumber;
	}

}

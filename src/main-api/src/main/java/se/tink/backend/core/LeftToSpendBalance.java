package se.tink.backend.core;

import org.joda.time.DateTime;

public class LeftToSpendBalance extends Balance {

	private String period;

	public LeftToSpendBalance (DateTime date, double amount, String period)
	{
		super (date, amount);
		this.period = period;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}
}

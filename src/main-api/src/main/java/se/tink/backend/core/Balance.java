package se.tink.backend.core;

import org.joda.time.DateTime;

public class Balance
{
	private DateTime date;
	private double amount;

	public Balance (DateTime date, double amount)
	{
		this.amount = amount;
		this.date = date;
	}
	
	public Balance (KVPair<String, Double> kvpair)
	{
		date = new DateTime(kvpair.getKey());
		amount = kvpair.getValue();
	}
	
	public Balance (StringDoublePair stringDoublePair)
	{
		date = new DateTime(stringDoublePair.getKey());
		amount = stringDoublePair.getValue();
	}

	public Balance () {}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public DateTime getDate() {
		return date;
	}

	public void setDate(DateTime date) {
		this.date = date;
	}
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import java.text.ParseException;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class ChargeDate{

	private int rawValue;
	private String formattedDate;

	public int getRawValue(){
		return rawValue;
	}

	public String getFormattedDate(){
		return formattedDate;
	}

	public Date toDate() {
		Date date;
		String dateString = String.valueOf(rawValue);
		try {
			date =
					DateUtils.flattenTime(
							DateUtils.flattenTime(
									ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.parse(dateString)));
		} catch (ParseException e) {
			String errorMessage = "Cannot parse date: " + dateString;
			throw new IllegalStateException(errorMessage, e);
		}
		return date;
	}
}
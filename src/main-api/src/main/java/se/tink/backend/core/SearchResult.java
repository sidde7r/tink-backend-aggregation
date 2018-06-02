package se.tink.backend.core;

import io.swagger.annotations.ApiModelProperty;

import io.protostuff.Tag;
import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;

@XmlRootElement
public class SearchResult {
	public static SearchResult fromBudget(Budget budget) {
		SearchResult result = new SearchResult();

		result.setType(SearchResultTypes.BUDGET);
		result.setBudget(budget);

		return result;
	}

	public static SearchResult fromTransaction(Transaction transaction) {
		SearchResult result = new SearchResult();

		result.setType(SearchResultTypes.TRANSACTION);
		result.setTransaction(transaction);
		result.setTimestamp(transaction.getDate());

		return result;
	}

	@JsonSerialize(include = Inclusion.NON_NULL)
	@Tag(1)
	@ApiModelProperty(name = "budget", hidden = true)
	protected Budget budget;
	@Tag(2)
	@ApiModelProperty(name = "score", hidden = true)
	protected double score;
	@Tag(3)
	@ApiModelProperty(name = "timestamp", hidden = true)
	protected Date timestamp;
	@JsonSerialize(include = Inclusion.NON_NULL)
	@Tag(4)
	@ApiModelProperty(name = "transaction", value="The transactions resulting from the query.", required = false)
	protected Transaction transaction;
	@Tag(5)
	@ApiModelProperty(name = "type", value="The search type.", example = "TRANSACTION", required = true)
	protected SearchResultTypes type;

	public Budget getBudget() {
		return budget;
	}

	public double getScore() {
		return score;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public SearchResultTypes getType() {
		return type;
	}

	public void setBudget(Budget budget) {
		this.budget = budget;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public void setType(SearchResultTypes type) {
		this.type = type;
	}
}

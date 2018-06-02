package se.tink.backend.common.search.parsers;

import java.util.List;

import se.tink.backend.common.search.SearchParserCommand;
import se.tink.backend.common.search.SearchParserContext;

public class LocationSearchParserCommand extends SearchParserCommand {
	@Override
	public List<String> parse(List<String> queryWords,
			SearchParserContext context, boolean addFilter) {
		return queryWords;
	}
}

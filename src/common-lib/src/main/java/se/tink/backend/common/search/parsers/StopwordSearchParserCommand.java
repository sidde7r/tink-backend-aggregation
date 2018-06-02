package se.tink.backend.common.search.parsers;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.common.search.SearchParserCommand;
import se.tink.backend.common.search.SearchParserContext;

/**
 * TODO: Move this to a configurable analyzer in ElasticSearch.
 */
public class StopwordSearchParserCommand extends SearchParserCommand {
    private static final ImmutableSet<String> STOPWORDS = ImmutableSet.of("i", "kr");

	@Override
	public List<String> parse(List<String> queryWords,
			SearchParserContext context, boolean addFilter) {
		List<String> filteredQueryWords = Lists.newArrayList();

		for (String queryWord : queryWords) {
			if (!STOPWORDS.contains(queryWord))
				filteredQueryWords.add(queryWord);
		}

		return filteredQueryWords;
	}

}

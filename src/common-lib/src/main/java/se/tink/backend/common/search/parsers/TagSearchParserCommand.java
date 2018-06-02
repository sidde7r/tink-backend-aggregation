package se.tink.backend.common.search.parsers;

import java.util.LinkedList;
import java.util.List;

import org.elasticsearch.index.query.FilterBuilders;

import se.tink.backend.common.search.SearchParserCommand;
import se.tink.backend.common.search.SearchParserContext;

public class TagSearchParserCommand extends SearchParserCommand {
    
    protected static final char HASHBANG = '#';

    @Override
    public List<String> parse(List<String> queryWords, SearchParserContext context, boolean addFilter) {
        List<String> resultingQueryWords = new LinkedList<String>();

          for (String word : queryWords) {  
            if (word.length() > 1 && word.charAt(0) == HASHBANG) {

                if (addFilter) {
                    context.getQueryFilters().add(FilterBuilders.termFilter("tags", word.substring(1).toLowerCase()));
                }
                continue;
            }
            resultingQueryWords.add(word);
        }
        return resultingQueryWords;
    }
}

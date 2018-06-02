package se.tink.backend.common.search.parsers;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Iterables;
import org.elasticsearch.index.query.FilterBuilders;

import se.tink.backend.common.search.SearchParserCommand;
import se.tink.backend.common.search.SearchParserContext;

import com.google.common.base.Splitter;
import com.google.common.primitives.Ints;

public class AmountSpanSearchParserCommand extends SearchParserCommand {
    
    private static Splitter SPLITTER = Splitter.on(",");
    
    @Override
    public List<String> parse(List<String> queryWords, SearchParserContext context, boolean addFilter) {
        List<String> resultingQueryWords = new LinkedList<String>();

        for (int i = 0; i < queryWords.size(); i++) {
            try {
                if (queryWords.get(i).equals(context.getCatalog().getString("over")) && i < queryWords.size()
                        && isInteger(queryWords.get(i + 1))) {

                    if (addFilter) {
                        context.getQueryFilters().add(
                                FilterBuilders.rangeFilter("amount").from(Integer.parseInt(queryWords.get(i + 1))));
                    }
                    i++;
                    continue;
                }
                
                if (queryWords.size() >= i + 2) {
                    String words = queryWords.get(i) + " " + queryWords.get(i + 1);
                    if (words.equals(context.getCatalog().getString("more than")) && i < queryWords.size()
                            && isInteger(queryWords.get(i + 2))) {

                        if (addFilter) {
                            context.getQueryFilters().add(
                                    FilterBuilders.rangeFilter("amount").from(Integer.parseInt(queryWords.get(i + 2))));
                        }
                        i+=2;
                        continue;
                    }
                    
                    if (words.equals(context.getCatalog().getString("less than")) && i < queryWords.size()
                            && isInteger(queryWords.get(i + 2))) {

                        if (addFilter) {
                            context.getQueryFilters().add(
                                    FilterBuilders.rangeFilter("amount").to(Integer.parseInt(queryWords.get(i + 2))));
                        }
                        i+=2;
                        continue;
                    }

                }

                if (queryWords.get(i).equals(context.getCatalog().getString("under")) && i < queryWords.size()
                        && isInteger(queryWords.get(i + 1))) {

                    if (addFilter) {
                        context.getQueryFilters().add(
                                FilterBuilders.rangeFilter("amount").to(Integer.parseInt(queryWords.get(i + 1))));
                    }
                    i++;
                    continue;
                }
                
                Iterable<String> aroundSynonyms = SPLITTER.split(context.getCatalog().getString("around")); 

                if (Iterables.contains(aroundSynonyms, queryWords.get(i)) && i < queryWords.size()
                        && isInteger(queryWords.get(i + 1))) {

                    if (addFilter) {
                        int amount = Ints.tryParse(queryWords.get(i + 1));
                        int fromAmount = (int) (amount - (amount * 0.05));
                        int toAmount = (int) (amount + (amount * 0.05));

                        context.getQueryFilters().add(
                                FilterBuilders.rangeFilter("amount").from(fromAmount).to(toAmount));
                    }
                    i++;
                    continue;
                }

                if (queryWords.size() == 1 && Ints.tryParse(queryWords.get(i)) != null && !YEARS.contains(queryWords.get(i))) {

                    if (addFilter) {
                        context.getQueryFilters().add(
                                FilterBuilders.rangeFilter("amount").from(Ints.tryParse(queryWords.get(i)))
                                        .to(Ints.tryParse(queryWords.get(i)) + 1).includeUpper(false));
                    }
                    continue;
                }

            } catch (IndexOutOfBoundsException e) {
                // NOOP.
            }

            resultingQueryWords.add(queryWords.get(i));
        }

        return resultingQueryWords;
    }

    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException nfe) {
        }
        return false;
    }
}

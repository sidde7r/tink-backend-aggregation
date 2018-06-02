package se.tink.backend.common.search;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class SearchParserCommand {
    protected static Set<String> YEARS = IntStream.rangeClosed(2008, LocalDate.now().getYear())
            .boxed()
            .map(y -> Integer.toString(y))
            .collect(Collectors.toSet());

    public abstract List<String> parse(List<String> queryWords,
            SearchParserContext context, boolean addFilter);
}

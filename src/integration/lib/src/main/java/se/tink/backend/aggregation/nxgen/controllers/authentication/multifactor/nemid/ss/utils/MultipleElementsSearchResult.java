package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils;

import java.util.Collections;
import java.util.List;
import lombok.Data;

@Data
public class MultipleElementsSearchResult {

    private final List<ElementsSearchResult> elementsSearchResults;

    public static MultipleElementsSearchResult of(
            List<ElementsSearchResult> elementsSearchResults) {
        return new MultipleElementsSearchResult(elementsSearchResults);
    }

    public static MultipleElementsSearchResult empty() {
        return new MultipleElementsSearchResult(Collections.emptyList());
    }
}

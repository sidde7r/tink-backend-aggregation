package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.screens;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class MitIdScreenQuery {

    private final List<MitIdScreen> expectedScreensToSearchFor;
    private final int searchForSeconds;

    public static MitIdScreensQueryBuilder builder() {
        return new MitIdScreensQueryBuilder();
    }

    public MitIdScreensQueryBuilder toBuilder() {
        return new MitIdScreensQueryBuilder(this);
    }

    public static class MitIdScreensQueryBuilder {

        private final List<MitIdScreen> screensToWaitFor;
        private int searchForSeconds;

        public MitIdScreensQueryBuilder() {
            screensToWaitFor = new ArrayList<>();
            searchForSeconds = 0;
        }

        public MitIdScreensQueryBuilder(MitIdScreenQuery screenQuery) {
            this.screensToWaitFor = screenQuery.getExpectedScreensToSearchFor();
            this.searchForSeconds = screenQuery.getSearchForSeconds();
        }

        public MitIdScreensQueryBuilder searchForExpectedScreens(MitIdScreen... screens) {
            screensToWaitFor.addAll(asList(screens));
            return this;
        }

        public MitIdScreensQueryBuilder searchForExpectedScreens(List<MitIdScreen> screens) {
            screensToWaitFor.addAll(screens);
            return this;
        }

        public MitIdScreensQueryBuilder searchForSeconds(int seconds) {
            this.searchForSeconds = seconds;
            return this;
        }

        public MitIdScreensQueryBuilder searchOnlyOnce() {
            this.searchForSeconds = 0;
            return this;
        }

        public MitIdScreenQuery build() {
            return new MitIdScreenQuery(screensToWaitFor, searchForSeconds);
        }
    }
}

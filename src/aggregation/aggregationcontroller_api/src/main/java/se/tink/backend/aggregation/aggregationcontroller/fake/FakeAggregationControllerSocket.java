package se.tink.backend.aggregation.aggregationcontroller.fake;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface FakeAggregationControllerSocket {}

package se.tink.analytics.spark.functions;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;
import se.tink.analytics.jobs.loandata.AggregatedAreaLoanDataExtended;
import se.tink.analytics.jobs.loandata.LoanData;
import se.tink.analytics.jobs.loandata.UserDemographics;
import se.tink.libraries.uuid.UUIDUtils;

@SuppressWarnings("serial")
public class SparkFunctions implements Serializable {

    public static class Mappers {
        public static final Function<UserDemographics, UUID> USER_DEMOGRAPHICS_TO_USERID = new Function<UserDemographics, UUID>() {
            @Override
            public UUID call(UserDemographics ud) throws Exception {
                return ud.getUserId();
            }
        };

        /**
         * Get the keys from tuples.
         *
         * @return
         */
        public static <K, V> Function<Tuple2<K, V>, K> tupleKeys() {
            return new Function<Tuple2<K, V>, K>() {
                @Override
                public K call(Tuple2<K, V> v) throws Exception {
                    return v._1();
                }
            };
        }

        public static final Function<LoanData, UUID> LOANS_BY_ACCOUNT =
                new Function<LoanData, UUID>() {
                    @Override
                    public UUID call(LoanData data) throws Exception {
                        return data.getAccountId();
                    }
                };

        public static final Function<Iterable<LoanData>, LoanData> GET_FIRST =
                new Function<Iterable<LoanData>, LoanData>() {

                    @Override
                    public LoanData call(Iterable<LoanData> data) throws Exception {
                        return Iterables.getFirst(data, null);
                    }
                };

        public static final Function<AggregatedAreaLoanDataExtended, String> AGGREGATED_AREA_LOAN_TO_USERID =
                new Function<AggregatedAreaLoanDataExtended, String>() {
                    @Override
                    public String call(AggregatedAreaLoanDataExtended a) {
                        return UUIDUtils.toTinkUUID(a.getUserId());
                    }
                };

        public static final Function<AggregatedAreaLoanDataExtended, String> AGGREGATED_LOAN_BY_AREA_AND_BANK =
                new Function<AggregatedAreaLoanDataExtended, String>() {

                    @Override
                    public String call(AggregatedAreaLoanDataExtended agg) throws Exception {
                        return String.valueOf(agg.getAreaId()) + "/" + agg.getBank();
                    }
                };
    }

    public static class Reducers {
        public static final Function2<Double, Double, Double> SUM_DOUBLES = new Function2<Double, Double, Double>() {
            @Override
            public Double call(Double d1, Double d2) throws Exception {
                return d1 + d2;
            }
        };

        public static final Function2<Integer, Integer, Integer> SUM_INTEGERS = new Function2<Integer, Integer, Integer>() {
            public Integer call(Integer a, Integer b) {
                return a + b;
            }
        };

        public static final Function2<AggregatedAreaLoanDataExtended, AggregatedAreaLoanDataExtended, AggregatedAreaLoanDataExtended> AGGREGATED_AREA_LOAN_SUM =
                new Function2<AggregatedAreaLoanDataExtended, AggregatedAreaLoanDataExtended, AggregatedAreaLoanDataExtended>() {
                    @Override
                    public AggregatedAreaLoanDataExtended call(AggregatedAreaLoanDataExtended o, AggregatedAreaLoanDataExtended o2)
                            throws Exception {

                        o.setAvgInterest(o.getAvgInterest() + o2.getAvgInterest());
                        o.setAvgBalance(o.getAvgBalance() + o2.getAvgBalance());

                        return o;
                    }
                };
    }

    public static class Filters {

        /**
         * Filter tuples with numeric values based on minimum value (exclusive!).
         *
         * @param floor
         * @return
         */
        public static <K, V extends Number> Function<Tuple2<K, V>, Boolean> valueGreaterThan(final V floor) {
            return new Function<Tuple2<K, V>, Boolean>() {
                @Override
                public Boolean call(Tuple2<K, V> tuple) throws Exception {
                    V value = tuple._2();

                    if (value.getClass() == Integer.class) {
                        return value.intValue() > floor.intValue();
                    } else if (value.getClass() == Float.class) {
                        return value.floatValue() > floor.floatValue();
                    } else if (value.getClass() == Long.class) {
                        return value.longValue() > floor.longValue();
                    } else if (value.getClass() == Short.class) {
                        return value.shortValue() > floor.shortValue();
                    } else if (value.getClass() == Byte.class) {
                        return value.byteValue() > floor.byteValue();
                    } else //if (value.getClass() == Double.class)
                    {
                        return value.doubleValue() > floor.doubleValue();
                    }
                }
            };
        }

        public static Function<UserDemographics, Boolean> getByMarket(final String marketCode) {
            return new Function<UserDemographics, Boolean>() {
                @Override
                public Boolean call(UserDemographics userDemographics) throws Exception {
                    if (Strings.isNullOrEmpty(userDemographics.getMarket())) {
                        return false;
                    }

                    return Objects.equals(userDemographics.getMarket(), marketCode);
                }
            };
        }

        public static Function<UserDemographics, Boolean> HAS_POSTAL_CODE = new Function<UserDemographics, Boolean>() {
            @Override
            public Boolean call(UserDemographics userDemographics) throws Exception {
                if (Strings.isNullOrEmpty(userDemographics.getPostalCode())) {
                    return false;
                }
                return userDemographics.getPostalCode().length() == 5;
            }
        };

        /**
         * Filter tuples with numeric values based on minimum value (inclusive!).
         *
         * @param value
         * @return
         */
        public static <K, V extends Number> Function<Tuple2<K, V>, Boolean> valueGreaterThanOrEqualTo(final V value) {
            return new Function<Tuple2<K, V>, Boolean>() {
                @Override
                public Boolean call(Tuple2<K, V> tuple) throws Exception {
                    return value.equals(tuple._2()) || Filters.<K, V>valueGreaterThan(value).call(tuple);
                }
            };
        }
    }
}

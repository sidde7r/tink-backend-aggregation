package se.tink.backend.common.location.transaction;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang.time.DateUtils;

import se.tink.backend.common.location.CityEstimator;
import se.tink.backend.common.location.CityLocationGuess;
import se.tink.backend.common.location.CityPredicate;
import se.tink.backend.common.location.LocationGuessType;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.libraries.date.ThreadSafeDateFormat;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;

public class TransactionBasedCityEstimator implements CityEstimator {

    public static final int DEFAULT_NUMBER_DAYS_RADIUS = 4;

    private DailyCityClusterer clusterer;
    private List<Transaction> userTransactions;

    public TransactionBasedCityEstimator(Map<String, String> merchantsById, List<Transaction> userTransactions) {
        this.userTransactions = userTransactions;
        this.clusterer = new DailyCityClusterer(merchantsById);
    }

    @Override
    public LocationGuessType getType() {
        return LocationGuessType.TRANSACTIONAL;
    }

    public List<CityLocationGuess> estimate(User user, Date targetDate) {
        return estimate(user, targetDate, DEFAULT_NUMBER_DAYS_RADIUS);
    }

    public List<CityLocationGuess> estimate(User user, final Date targetDate, final int daysRadius) {

        List<DailyCityExistence> existences = clusterer.transactionsPerDayPerCity(userTransactions, targetDate, daysRadius);

        ListMultimap<String, DailyCityExistence> existencesByDay = Multimaps.index(existences, new Function<DailyCityExistence, String>() {
            @Nullable
            @Override
            public String apply(DailyCityExistence e) {
                return e.getDateString();
            }
        });

        List<DailyCityExistence> existencesOnTarget = existencesByDay.get(ThreadSafeDateFormat.FORMATTER_DAILY.format(targetDate));

        if (existencesOnTarget.size() > 0) {
            return locationGuessForOneDay(existencesOnTarget);
        } else {
            return locationGuessMerged(existencesByDay, targetDate, daysRadius);
        }
    }

    private List<CityLocationGuess> locationGuessForOneDay(List<DailyCityExistence> existencesForOneDay) {

        int totalTransactions = countTransactions(existencesForOneDay);
        if (totalTransactions == 0) {
            return Lists.newArrayList();
        }

        List<CityLocationGuess> guesses = Lists.newArrayList();

        for(DailyCityExistence existence : existencesForOneDay) {
            float prob = (float)existence.getNumTransactions() / (float)totalTransactions;
            guesses.add(createGuess(existence.getCity(), prob));
        }
        return guesses;
    }

    private List<CityLocationGuess> locationGuessMerged(ListMultimap<String, DailyCityExistence> existencesByDay, Date targetDate, int radius) {

        List<DailyCityExistence> before = findClosest (existencesByDay, targetDate, radius, true);
        List<DailyCityExistence> after = findClosest (existencesByDay, targetDate, radius, false);

        List<CityLocationGuess> guessesBefore = null;
        List<CityLocationGuess> guessesAfter = null;


        if (before != null) {
            guessesBefore = locationGuessForOneDay(before);
        }

        if (after != null) {
            guessesAfter = locationGuessForOneDay(after);
        }

        if (guessesBefore != null && guessesAfter != null) {
            return mergeLocationGuesses(guessesBefore, guessesAfter);
        } else if (guessesBefore != null) {
            return guessesBefore;
        } else if (guessesAfter != null) {
            return guessesAfter;
        } else {
            return Lists.newArrayList();
        }
    }

    private List<CityLocationGuess> mergeLocationGuesses(List<CityLocationGuess> guessesBefore, List<CityLocationGuess> guessesAfter) {
        List<CityLocationGuess> guessesAfterCopy = Lists.newArrayList(guessesAfter);
        List<CityLocationGuess> merged = Lists.newArrayList();

        for(CityLocationGuess before : guessesBefore) {
            CityLocationGuess after = Iterables.find(guessesAfterCopy, new CityPredicate(before.getCity()), null);
            if (after == null) {
                merged.add(createGuess(before.getCity(), before.getProbability() / 2f));
            } else {
                merged.add(createGuess(before.getCity(), (before.getProbability() + after.getProbability()) / 2f));
                guessesAfterCopy.remove(after);
            }
        }
        for(CityLocationGuess after: guessesAfterCopy) {
            merged.add(createGuess(after.getCity(), after.getProbability() / 2f));
        }
        return merged;
    }

    private List<DailyCityExistence> findClosest(ListMultimap<String, DailyCityExistence> existencesByDay, Date targetDate, int radius, boolean before) {

        int direction = before ? -1 : 1;
        for(int i = direction; Math.abs(i) <= radius; i = i + direction) {
            Date date = DateUtils.addDays(targetDate, i);
            String key = ThreadSafeDateFormat.FORMATTER_DAILY.format(date);

            if (existencesByDay.containsKey(key)) {
                return existencesByDay.get(key);
            }
        }
        return null;
    }

    private int countTransactions(List<DailyCityExistence> existences) {
        int count = 0;
        for(DailyCityExistence e : existences) {
            count += e.getNumTransactions();
        }
        return count;
    }

    private CityLocationGuess createGuess(String city, float probability) {
        CityLocationGuess guess = new CityLocationGuess(getType());
        guess.setProbability(probability);
        guess.setCity(city);
        return guess;
    }
}

package se.tink.backend.system.cli.statistics.market;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Strings;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.system.cli.CliPrintUtils;

public class StatisticsPrinter {

    // Prints statistics by provider, sorted in descending order on number of credentials count
    static void printProviderUserCountsTable(Map<String, Integer> totalCountByProviderName,
            Map<String, Map<String, Integer>> credStatusCountByProviderName,
            Map<String, String> agentNameByProviderName) {
        List<Map<String, String>> output = Lists.newArrayList();

        totalCountByProviderName.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .forEach(entry -> {
                    Map<String, String> tableRow = new LinkedHashMap<>();
                    tableRow.put("Provider", entry.getKey());
                    tableRow.put("Agent", agentNameByProviderName.get(entry.getKey()));
                    tableRow.put("Number of credentials", String.valueOf(entry.getValue()));

                    Map<String, Integer> countByCredentialStatus = credStatusCountByProviderName.get(entry.getKey());
                    countByCredentialStatus.forEach((key, value) -> tableRow.put(key, String.valueOf(value)));

                    output.add(tableRow);
                });


        CliPrintUtils.printTable(output);
    }

    static void printMarketUserNumbers(UserRepository userRepository, String market, String dateInput, int userCount) {
        int totalUserCount = (int) userRepository.countByProfileMarket(market);

        if (Strings.isNullOrEmpty(dateInput)) {
            System.out.println(String.format("Number of users in market %s: %d", market.toUpperCase(), totalUserCount));
            return;
        }

        System.out.println(String.format(
                "Number of users in market %s created from %s: %d (total number of users in market: %d)",
                market.toUpperCase(), dateInput, userCount, totalUserCount));
    }
}

package se.tink.backend.aggregation.agents.summary.refresh;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.libraries.credentials.service.RefreshableItem;

@Slf4j
@Getter(AccessLevel.PACKAGE)
@Setter(AccessLevel.PACKAGE)
public class RefreshSummary {

    private static final String UPDATE_SUMMARY_ERROR_MSG =
            "[REFRESH SUMMARY] Updating item summary cannot be performed. At least one of the args is null.";
    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                    .create();

    private RefreshStatus status = RefreshStatus.NOT_STARTED;
    private List<RefreshableItemSummary> itemSummaries = new ArrayList<>();

    public void updateStatus(RefreshStatus status) {
        this.status = status;
    }

    public void addItemSummary(RefreshableItem item) {
        addItemSummary(item, RefreshableItemFetchingStatus.REQUESTED);
    }

    public void addItemSummary(RefreshableItem item, RefreshableItemFetchingStatus status) {
        if (!ObjectUtils.allNotNull(item, status)) {
            log.warn(
                    "[REFRESH SUMMARY] addItemSummary cannot be performed. item: {}, status: {}",
                    item == null,
                    status == null);
            return;
        }

        itemSummaries.add(new RefreshableItemSummary(item, status, null, null));
    }

    public void updateItemSummary(RefreshableItem item, RefreshableItemFetchingStatus status) {
        if (!ObjectUtils.allNotNull(item, status)) {
            log.warn(UPDATE_SUMMARY_ERROR_MSG);
            return;
        }

        itemSummaries.stream()
                .filter(itemSummary -> itemSummary.getItem() == item)
                .findFirst()
                .ifPresent(itemSummary -> itemSummary.setFetchingStatus(status));
    }

    public void updateItemSummary(
            RefreshableItem item, RefreshableItemFetchingStatus status, int fetchingCounter) {
        if (!ObjectUtils.allNotNull(item, status, fetchingCounter)) {
            log.warn(UPDATE_SUMMARY_ERROR_MSG);
            return;
        }

        itemSummaries.stream()
                .filter(itemSummary -> itemSummary.getItem() == item)
                .findFirst()
                .ifPresent(
                        itemSummary -> {
                            itemSummary.setFetchingStatus(status);
                            itemSummary.setFetched(Collections.singletonList(fetchingCounter));
                        });
    }

    public void updateItemSummary(
            RefreshableItem item,
            RefreshableItemFetchingStatus status,
            List<Integer> fetchingCounters) {
        if (!ObjectUtils.allNotNull(item, status, fetchingCounters)) {
            log.warn(UPDATE_SUMMARY_ERROR_MSG);
            return;
        }

        itemSummaries.stream()
                .filter(itemSummary -> itemSummary.getItem() == item)
                .findFirst()
                .ifPresent(
                        itemSummary -> {
                            itemSummary.setFetchingStatus(status);
                            itemSummary.setFetched(fetchingCounters);
                        });
    }

    public void updateItemSummary(
            RefreshableItem item,
            RefreshableItemFetchingStatus status,
            List<Integer> fetchingCounters,
            LocalDate oldestTrxDate) {
        if (!ObjectUtils.allNotNull(item, status, fetchingCounters)) {
            log.warn(UPDATE_SUMMARY_ERROR_MSG);
            return;
        }

        itemSummaries.stream()
                .filter(itemSummary -> itemSummary.getItem() == item)
                .findFirst()
                .ifPresent(
                        itemSummary -> {
                            itemSummary.setFetchingStatus(status);
                            itemSummary.setFetched(fetchingCounters);
                            itemSummary.setOldestTransactionDate(oldestTrxDate);
                        });
    }

    public String toJson() {
        String json = EMPTY;
        try {
            json = GSON.toJson(this);
        } catch (Exception e) {
            log.error("[REFRESH SUMMARY] Converting summary to json failed.", e);
        }
        return json;
    }

    static class LocalDateAdapter implements JsonSerializer<LocalDate> {

        public JsonElement serialize(
                LocalDate date, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
    }
}

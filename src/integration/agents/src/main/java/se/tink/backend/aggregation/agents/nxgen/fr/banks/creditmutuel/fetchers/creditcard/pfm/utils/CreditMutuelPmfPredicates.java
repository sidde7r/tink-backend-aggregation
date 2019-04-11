package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.utils;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.entities.SubItemsEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.entities.ValueEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.rpc.CreditCardResponse;

public class CreditMutuelPmfPredicates {

    public static Function<CreditCardResponse, Stream<SubItemsEntity>>
            getSubItemsValueStreamFromResponse =
                    c ->
                            c.getView().getSections().stream()
                                    .flatMap(s -> s.getItems().stream().map(x -> x.getSubItems()));

    public static Function<CreditCardResponse, Stream<ValueEntity>> getItemEntitiesFromResponse =
            r ->
                    r.getView().getSections().stream()
                            .flatMap(
                                    s ->
                                            s.getItems().stream()
                                                    .flatMap(x -> x.getOutput().stream()));

    public static Function<CreditCardResponse, Stream<ValueEntity>> getValueEntitiesFromEachInput =
            r ->
                    r.getView().getSections().stream()
                            .flatMap(
                                    s ->
                                            s.getItems().stream()
                                                    .flatMap(x -> x.getOutput().stream()));

    public static Predicate<ValueEntity> filterValueEntityByName(String positionName) {
        return v -> positionName.equalsIgnoreCase(v.getPosition());
    }

    public static Predicate<ValueEntity> filterValueEntityByType(String typeName) {
        return v -> typeName.equalsIgnoreCase(v.getType());
    }
}

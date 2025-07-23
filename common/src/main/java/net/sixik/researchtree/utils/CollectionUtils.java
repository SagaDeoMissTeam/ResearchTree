package net.sixik.researchtree.utils;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CollectionUtils {

    public static <T> boolean contains(Collection<T> collection, Predicate<T> tPredicate) {
        for (T t : collection) {
            if(tPredicate.test(t)) return true;
        }

        return false;
    }

    public static <T> boolean contains(Collection<T> collection, Predicate<T> tPredicate, Consumer<T> func) {
        for (T t : collection) {
            if(tPredicate.test(t)) {
                func.accept(t);
                return true;
            }
        }

        return false;
    }
}

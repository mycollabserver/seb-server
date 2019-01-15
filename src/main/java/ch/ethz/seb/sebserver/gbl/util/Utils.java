/*
 * Copyright (c) 2018 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.gbl.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

import ch.ethz.seb.sebserver.gbl.Constants;

public final class Utils {

    public static <T> Collection<T> immutableCollectionOf(final Collection<T> collection) {
        return (collection != null)
                ? Collections.unmodifiableCollection(collection)
                : Collections.emptySet();
    }

    /** Use this to create an immutable Collection of specified type from varargs
     *
     * @param values elements of the new immutable Collection
     * @return an immutable Collection of specified type with given elements */
    @SafeVarargs
    public static <T> Collection<T> immutableCollectionOf(final T... values) {
        if (values == null || values.length <= 0) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableCollection(Arrays.asList(values));
    }

    public static <T> Set<T> immutableSetOf(final Set<T> set) {
        return (set != null)
                ? Collections.unmodifiableSet(set)
                : Collections.emptySet();
    }

    /** Use this to create an immutable Set of specified type from varargs
     *
     * @param values elements of the new immutable Set
     * @return an immutable Set of specified type with given elements */
    @SafeVarargs
    public static <T> Set<T> immutableSetOf(final T... items) {
        if (items == null || items.length <= 0) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(items)));
    }

    public static <T> List<T> asImmutableList(final T[] array) {
        return (array != null)
                ? Collections.unmodifiableList(Arrays.asList(array))
                : Collections.emptyList();
    }

    public static <T extends Enum<T>> Collection<Tuple<String>> createSelectionResource(final Class<T> enumClass) {
        return Collections.unmodifiableCollection(Arrays.asList(
                enumClass.getEnumConstants())
                .stream()
                .map(e -> new Tuple<>(e.name(), e.name()))
                .collect(Collectors.toList()));
    }

    public static String toSQLWildcard(final String text) {
        return (text == null) ? null : "%" + text + "%";
    }

    public static Result<Long> dateTimeStringToTimestamp(final String startTime) {
        return Result.tryCatch(() -> {
            return DateTime.parse(startTime, Constants.DATE_TIME_PATTERN_UTC_NO_MILLIS).getMillis();
        });
    }

    public static Long dateTimeStringToTimestamp(final String startTime, final Long defaultValue) {
        return dateTimeStringToTimestamp(startTime)
                .getOrElse(defaultValue);
    }

    public static <M extends Map<K, V>, K, V> M mapPut(final M map, final K key, final V value) {
        map.put(key, value);
        return map;
    }

    public static <M extends Map<K, V>, K, V> M mapPutAll(final M map1, final M map2) {
        map1.putAll(map2);
        return map1;
    }

    public static <M extends Map<K, Collection<V>>, K, V> M mapCollect(final M map, final K key, final V value) {
        final List<V> list = (List<V>) map.computeIfAbsent(key, k -> new ArrayList<>());
        list.add(value);
        return map;
    }

}

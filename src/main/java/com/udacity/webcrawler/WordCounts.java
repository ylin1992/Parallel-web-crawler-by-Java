package com.udacity.webcrawler;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class that sorts the map of word counts.
 *
 * <p>TODO: Reimplement the sort() method using only the Stream API and lambdas and/or method
 * references.
 */
final class WordCounts {

    /**
     * Given an unsorted map of word counts, returns a new map whose word counts are sorted according
     * to the provided {@link WordCountComparator}, and includes only the top
     * {@param popluarWordCount} words and counts.
     *
     * <p>TODO: Reimplement this method using only the Stream API and lambdas and/or method
     * references.
     *
     * @param wordCounts       the unsorted map of word counts.
     * @param popularWordCount the number of popular words to include in the result map.
     * @return a map containing the top {@param popularWordCount} words and counts in the right order.
     */
    static Map<String, Integer> sort(Map<String, Integer> wordCounts, int popularWordCount) {

        // TODO: Reimplement this method using only the Stream API and lambdas and/or method references.

        return wordCounts
                .entrySet()
                .stream()
                .sorted(new WordCountComparator())
                // Stream#.limit(long) restricts the max size of a given collector
                // reference: https://howtodoinjava.com/java8/java-stream-limit-method-example/
                .limit(Math.min(popularWordCount, wordCounts.size()))
                // use supplier to generate a LinkedHashMap that can store the order of the sorted map
                // this can avoid the wrong-order failure
                // reference: https://dzone.com/articles/how-to-sort-a-map-by-value-in-java-8
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o, n) -> o, LinkedHashMap::new));

    }

    /**
     * A {@link Comparator} that sorts word count pairs correctly:
     *
     * <p>
     * <ol>
     *   <li>First sorting by word count, ranking more frequent words higher.</li>
     *   <li>Then sorting by word length, ranking longer words higher.</li>
     *   <li>Finally, breaking ties using alphabetical order.</li>
     * </ol>
     */
    private static final class WordCountComparator implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
            if (!a.getValue().equals(b.getValue())) {
                return b.getValue() - a.getValue();
            }
            if (a.getKey().length() != b.getKey().length()) {
                return b.getKey().length() - a.getKey().length();
            }
            return a.getKey().compareTo(b.getKey());
        }
    }

    private WordCounts() {
        // This class cannot be instantiated
    }
}

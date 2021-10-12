package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on a
 * {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
 */
final class ParallelWebCrawler implements WebCrawler {
    private final Clock clock;
    private final Duration timeout;
    private final int popularWordCount;
    private final ForkJoinPool pool;
    private final int maxDepth;
    private final List<Pattern> ignoredUrls;
    private final PageParserFactory parserFactory;

    @Inject
    ParallelWebCrawler(
            Clock clock,
            @Timeout Duration timeout,
            @PopularWordCount int popularWordCount,
            @TargetParallelism int threadCount,
            @MaxDepth int maxDepth,
            @IgnoredUrls List<Pattern> ignoredUrls,
            PageParserFactory parserFactory) {
        this.clock = clock;
        this.timeout = timeout;
        this.popularWordCount = popularWordCount;
        this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
        this.maxDepth = maxDepth;
        this.ignoredUrls = ignoredUrls;
        this.parserFactory = parserFactory;
    }

    @Override
    public CrawlResult crawl(List<String> startingUrls) {
        // modify sequential code to concurrent mode
        Instant deadline = clock.instant().plus(timeout);
        ConcurrentMap<String, Integer> counts = new ConcurrentHashMap<>();
        ConcurrentSkipListSet<String> visitedUrls = new ConcurrentSkipListSet<>();

        // loop through all urls and invoke a new internal task object
        for (String link : startingUrls) {
            pool.invoke(new CrawlInternal(link, deadline, maxDepth, counts, visitedUrls));
        }

        if (counts.isEmpty()) {
            return new CrawlResult.Builder()
                    .setWordCounts(counts)
                    .setUrlsVisited(visitedUrls.size())
                    .build();
        }

        return new CrawlResult.Builder()
                .setWordCounts(WordCounts.sort(counts, popularWordCount))
                .setUrlsVisited(visitedUrls.size())
                .build();
    }

    @Override
    public int getMaxParallelism() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * the CrawlInternal extending RecursiveTask implements crawlInternal()'s logic
     */
    public class CrawlInternal extends RecursiveAction {
        private final String url;
        private final Instant deadline;
        private final int maxDepth;
        // Use concurrent collections to avoid synchronizing issues
        private final ConcurrentMap<String, Integer> counts;
        private final ConcurrentSkipListSet<String> visitedUrls;

        public CrawlInternal(String url,
                             Instant deadline,
                             int maxDepth,
                             ConcurrentMap<String, Integer> counts,
                             ConcurrentSkipListSet<String> visitedUrls) {
            this.url = url;
            this.deadline = deadline;
            this.maxDepth = maxDepth;
            this.counts = counts;
            this.visitedUrls = visitedUrls;
        }

        protected void compute() {
            if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
                return;
            }
            for (Pattern pattern : ignoredUrls) {
                if (pattern.matcher(url).matches()) {
                    return;
                }
            }
            if (visitedUrls.contains(url)) {
                return;
            }
            visitedUrls.add(url);
            PageParser.Result result = parserFactory.get(url).parse();

            // revise recursive codes to an atomic operation
            for (ConcurrentMap.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
                // sequential codes
                //if (counts.containsKey(e.getKey())) {
                //    counts.put(e.getKey(), e.getValue() + counts.get(e.getKey()));
                //} else {
                //    counts.put(e.getKey(), e.getValue());
                //}

                // atomic operation
                //System.out.println("Working thread: #" + Thread.currentThread());
                counts.compute(e.getKey(), (k, v) -> v == null ? e.getValue() : e.getValue() + v);
            }


            List<CrawlInternal> subtasks = new ArrayList<>();
            List<String> links = result.getLinks();
            if (links != null) {
                subtasks = links
                        .stream()
                        .map(link -> new CrawlInternal(link, deadline, maxDepth - 1, counts, visitedUrls))
                        .collect(Collectors.toList());
            }
            invokeAll(subtasks);
        }
    }

}

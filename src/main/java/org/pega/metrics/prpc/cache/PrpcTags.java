package org.pega.metrics.prpc.cache;

import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import io.micrometer.core.instrument.Tags;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class encapsulates a cache store for {@code Tags} objects.
 * The cache is needed for correct functioning of ${@code PrpcCallback} objects.
 * As the cache contains hard references to {@code Tags} objects it could be potential memory leak spot
 * in case of inappropriate usage.
 * See PrpcTagsTest#memoryBenchmark test for more details about memory footprint.
 *
 * @author Alexey Lapin
 * @see org.pega.metrics.prpc.source.PrpcCallback
 * @see org.pega.metrics.prpc.source.AbstractPrpcSource#groupResults(ClipboardProperty, ClipboardProperty)
 */
public class PrpcTags {

    private static final String PREFIX_TAGS = "tags";

    private static final AtomicLong NEXT_ID = new AtomicLong(0);

    private static final Map<Tags, String> cache = new ConcurrentHashMap<>();

    private PrpcTags() {
    }

    // visible for testing
    static Map<Tags, String> getCache() {
        return cache;
    }

    /**
     * @param prop of Value Group type containing tag data
     * @return corresponding Tags object
     */
    @SuppressWarnings("unchecked")
    public static Tags of(ClipboardProperty prop) {
        Tags tags = Tags.empty();
        if (prop != null) {
            for (ClipboardProperty item : (Iterable<ClipboardProperty>) prop) {
                tags = tags.and(item.getName(), item.getStringValue());
            }
        }
        return tags;
    }

    /**
     * @param tags Tags object
     * @return unique id for provided tags representation
     */
    public static String id(Tags tags) {
        return cache.computeIfAbsent(tags, t -> PREFIX_TAGS + NEXT_ID.incrementAndGet());
    }

    /**
     * @param prop of Value Group type containing tag data
     * @return unique id for provided tags representation
     */
    public static String id(ClipboardProperty prop) {
        return id(of(prop));
    }
}

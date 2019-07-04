package ru.sbrf.pegi18.mon.prpc.source;

import com.google.common.base.Stopwatch;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.pega.pegarules.data.external.clipboard.ClipboardPageFactory;
import com.pega.pegarules.data.internal.clipboard.ClipboardPropertyFactory;
import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tags;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sbrf.pegi18.mon.prpc.TagsUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;


public class SourceManager {

    private static final Logger logger = LogManager.getLogger(SourceManager.class);

    private static final Supplier<ClipboardProperty> DEFAULT_NOOP_PROPERTY_SUPPLIER = () -> ClipboardPropertyFactory.getMostSuitableClipboardObject("Noop", 's', ClipboardPageFactory.acquire());

    private final ClipboardProperty NOOP_PROPERTY;
    private final Cache<Integer, PrpcSource> cache = CacheBuilder.newBuilder().build();
    private Supplier<ClipboardProperty> noopPropertySupplier;

    SourceManager() {
        this(DEFAULT_NOOP_PROPERTY_SUPPLIER);
    }

    SourceManager(Supplier<ClipboardProperty> noopPropertySupplier) {
        NOOP_PROPERTY = noopPropertySupplier.get();
    }

    public static SourceManager getInstance() {
        return Holder.instance;
    }

    public Cache<Integer, PrpcSource> getCache() {
        return cache;
    }

    public ToDoubleFunction<SourceManager> callback(int sourceHash, Tags tags, String valuePropertyReference) {
        return sourceMan -> {
            Stopwatch watch = logger.isDebugEnabled() ? Stopwatch.createStarted() : null;

            double value = Double.NaN;

            PrpcSource source = cache.getIfPresent(sourceHash);
            if (source == null) return value;

            ClipboardProperty obtained = source.get().orElse(NOOP_PROPERTY);
            if (obtained.isGroup() && !obtained.isEmpty()) {
                try {
                    ClipboardPage page = obtained.getPageValue(TagsUtils.id(tags));
                    ClipboardProperty valueProperty = page.getIfPresent(valuePropertyReference);
                    if (valueProperty != null) value = valueProperty.toDouble();
                } catch (Exception ex) {
                    logger.error("Failed:" + ex.getMessage());
                }
            }

            logger.debug(() -> "spent: " + (watch != null ? watch.toString() : ""));
            return value;
        };
    }

    public ToDoubleFunction<SourceManager> callback(PrpcSource source, Tags tags, String valuePropertyReference) {
        return new PrpcCallback(source, tags, valuePropertyReference);
    }

    @SuppressWarnings("unchecked")
    public Iterable<MultiGauge.Row> rows(int sourceHash, String valuePropertyReference) {
        List<MultiGauge.Row> rows = Collections.emptyList();

        PrpcSource source = cache.getIfPresent(sourceHash);
        if (source == null) return rows;

        ClipboardProperty obtained = source.get().orElse(NOOP_PROPERTY);
        if (obtained.isGroup() && !obtained.isEmpty()) {
            rows = new LinkedList<>();
            for (ClipboardProperty item : (Iterable<ClipboardProperty>) obtained) {
                Tags tags = TagsUtils.propToTags(item.getProperty("Tag"));
                rows.add(MultiGauge.Row.of(tags, this, callback(sourceHash, tags, valuePropertyReference)));
            }
        }
        return rows;
    }

    @SuppressWarnings("unchecked")
    public Iterable<MultiGauge.Row> rows(PrpcSource source, String valuePropertyReference) {
        List<MultiGauge.Row> rows = Collections.emptyList();

        ClipboardProperty obtained = source.get().orElse(NOOP_PROPERTY);
        if (obtained.isGroup() && !obtained.isEmpty()) {
            rows = new LinkedList<>();
            for (ClipboardProperty item : (Iterable<ClipboardProperty>) obtained) {
                Tags tags = TagsUtils.propToTags(item.getProperty("Tag"));
                rows.add(MultiGauge.Row.of(tags, this, callback(source, tags, valuePropertyReference)));
            }
        }
        return rows;
    }

    private static class Holder {
        private static SourceManager instance = new SourceManager();
    }
}

package ru.sbrf.pegi18.mon.prpc.source;

import com.google.common.base.Stopwatch;
import com.pega.pegarules.data.external.clipboard.ClipboardPageFactory;
import com.pega.pegarules.data.internal.clipboard.ClipboardPropertyFactory;
import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import io.micrometer.core.instrument.Tags;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.sbrf.pegi18.mon.prpc.TagsUtils;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

/**
 * Created by sp00x on 04.07.2019.
 * Project: micrometer-prpc
 */
public class PrpcCallback implements ToDoubleFunction<SourceManager> {

    private static final Logger logger = LogManager.getLogger(PrpcCallback.class);

    private static final Supplier<ClipboardProperty> DEFAULT_NOOP_PROPERTY_SUPPLIER = () -> ClipboardPropertyFactory.getMostSuitableClipboardObject("Noop", 's', ClipboardPageFactory.acquire());

    private static final ClipboardProperty NOOP_PROPERTY = DEFAULT_NOOP_PROPERTY_SUPPLIER.get();

    private final PrpcSource source;
    private final Tags tags;
    private final String valuePropertyReference;

    public PrpcCallback(PrpcSource source, Tags tags, String valuePropertyReference) {
        this.source = Objects.requireNonNull(source);
        this.tags = tags;
        this.valuePropertyReference = valuePropertyReference;
    }

    @Override
    public double applyAsDouble(SourceManager manager) {
        Stopwatch watch = logger.isDebugEnabled() ? Stopwatch.createStarted() : null;

        double value = Double.NaN;

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
    }
}

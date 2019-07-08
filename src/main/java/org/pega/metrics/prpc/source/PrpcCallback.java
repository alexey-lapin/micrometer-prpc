package org.pega.metrics.prpc.source;

import com.google.common.base.Stopwatch;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import io.micrometer.core.instrument.Tags;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pega.metrics.prpc.PrpcTags;

import java.util.function.ToDoubleFunction;

/**
 * @author Alexey Lapin
 */
public class PrpcCallback implements ToDoubleFunction<PrpcSource> {

    private static final Logger logger = LogManager.getLogger(PrpcCallback.class);

    @SuppressWarnings("FieldCanBeLocal")
    private final PrpcSource source;
    private final Tags tags;
    private final String valuePropName;

    private PrpcCallback(PrpcSource source, Tags tags, String valuePropName) {
        this.source = source;
        this.tags = tags;
        this.valuePropName = valuePropName;
    }

    public static PrpcCallback strong(PrpcSource source, String valuePropName) {
        return strong(source, null, valuePropName);
    }

    public static PrpcCallback strong(PrpcSource source, Tags tags, String valuePropName) {
        return new PrpcCallback(source, tags, valuePropName);
    }

    public static PrpcCallback weak(String valuePropName) {
        return weak(null, valuePropName);
    }

    public static PrpcCallback weak(Tags tags, String valuePropName) {
        return new PrpcCallback(null, tags, valuePropName);
    }

    @Override
    public double applyAsDouble(PrpcSource source) {
        Stopwatch watch = logger.isDebugEnabled() ? Stopwatch.createStarted() : null;

        double value = Double.NaN;

        ClipboardProperty obtained = source.get().orElse(null);
        try {
            if (obtained != null) {
                ClipboardProperty valueProperty = null;
                if (obtained.isGroup() && !obtained.isEmpty()) {
                    valueProperty = obtained.getPageValue(PrpcTags.id(tags)).getIfPresent(valuePropName);
                } else if (obtained.isPage()) {
                    valueProperty = obtained.getPageValue().getIfPresent(valuePropName);
                }
                if (valueProperty != null) value = valueProperty.toDouble();
            }
        } catch (Exception ex) {
            logger.error("Callback for '{}' and tags '{}' failed: '{}'", source, tags, ex.getMessage());
        }

        logger.debug("Callback for '{}' and tags '{}' finished - spent: {}",
            () -> source, () -> tags, () -> watch != null ? watch.toString() : "null");
        return value;
    }
}

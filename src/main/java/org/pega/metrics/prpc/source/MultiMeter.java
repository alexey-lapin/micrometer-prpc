package org.pega.metrics.prpc.source;

import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tags;
import org.pega.metrics.prpc.PrpcTags;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MultiMeter {

    private MultiMeter() {
    }

    @SuppressWarnings("unchecked")
    public static Iterable<MultiGauge.Row<?>> rows(PrpcSource source, String valuePropName) {
        List<MultiGauge.Row<?>> rows = Collections.emptyList();

        ClipboardProperty obtained = source.get().orElse(null);
        if (obtained != null && obtained.isGroup() && !obtained.isEmpty()) {
            rows = new LinkedList<>();
            for (ClipboardProperty item : (Iterable<ClipboardProperty>) obtained) {
                Tags tags = PrpcTags.of(item.getProperty(((AbstractPrpcSource) source).tagsPropName()));
                rows.add(MultiGauge.Row.of(tags, source, PrpcCallback.weak(tags, valuePropName)));
            }
        }
        return rows;
    }
}

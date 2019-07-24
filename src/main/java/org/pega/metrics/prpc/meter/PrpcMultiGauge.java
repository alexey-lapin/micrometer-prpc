package org.pega.metrics.prpc.meter;

import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tags;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.pega.metrics.prpc.cache.PrpcTags;
import org.pega.metrics.prpc.source.AbstractPrpcSource;
import org.pega.metrics.prpc.source.PrpcCallback;
import org.pega.metrics.prpc.source.PrpcSource;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.ToDoubleFunction;

public class PrpcMultiGauge extends AbstractPrpcMultiMeter {

    private final String valuePropName;

    PrpcMultiGauge(PrpcMultiGaugeBuilder builder) {
        super(builder);
        this.valuePropName = builder.valuePropName;
    }

    @Override
    protected void registerMeter(Meter.Id rowId, AbstractRow row) {
        GaugeRow gaugeRow = (GaugeRow) row;
        getRegistry().gauge(rowId.getName(), row.getTags(), getSource(), gaugeRow.valueFunction);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Iterable<AbstractRow> rows(PrpcSource source) {
        List<AbstractRow> rows = Collections.emptyList();

        ClipboardProperty obtained = source.get().orElse(null);
        if (obtained != null && obtained.isGroup() && !obtained.isEmpty()) {
            rows = new LinkedList<>();
            for (ClipboardProperty item : (Iterable<ClipboardProperty>) obtained) {
                Tags tags = PrpcTags.of(item.getProperty(((AbstractPrpcSource) source).tagsPropName()));
                rows.add(new GaugeRow(tags, PrpcCallback.strong(source, tags, valuePropName)));
            }
        }
        return rows;
    }

    public static PrpcMultiGaugeBuilder builder(String name) {
        return new PrpcMultiGaugeBuilder().name(name);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(valuePropName)
                .hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (other.getClass() != getClass()) return false;

        PrpcMultiGauge builder = (PrpcMultiGauge) other;
        return new EqualsBuilder()
                .appendSuper(super.equals(other))
                .append(valuePropName, builder.valuePropName)
                .isEquals();
    }

    public static class GaugeRow extends AbstractRow {
        private ToDoubleFunction<PrpcSource> valueFunction;

        GaugeRow(Tags tags, ToDoubleFunction<PrpcSource> valueFunction) {
            super(tags);
            this.valueFunction = valueFunction;
        }
    }

    public static class PrpcMultiGaugeBuilder extends AbstractPrpcMultiMeterBuilder<PrpcMultiGaugeBuilder> {

        private String valuePropName;

        public PrpcMultiGaugeBuilder valuePropName(String valuePropName) {
            this.valuePropName = valuePropName;
            return self();
        }

        @Override
        protected PrpcMultiGaugeBuilder self() {
            return this;
        }

        @Override
        public PrpcMultiGauge build() {
            type(Meter.Type.GAUGE);
            return new PrpcMultiGauge(this);
        }
    }
}

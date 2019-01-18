package ru.sbrf.pegi18.mon.prpc.meter;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import io.micrometer.core.instrument.Tags;

/**
 * @author Alexey Lapin
 */
public class PrpcFunctionCounter extends AbstractPrpcMeter {

    private String totalPropName;

    private PrpcFunctionCounter(PrpcFunctionCounterBuilder builder) {
        super(builder);
        this.totalPropName = builder.totalPropName;
    }

    public String getTotalPropName() {
        return totalPropName;
    }

    @Override
    public StringBuilder seriefy(StringBuilder buf, ClipboardPage page) {
        buf.append(namify(getId()));
        buf.append(tagify(Tags.of(propToTags(page.getIfPresent(getTagsPropName()))).and(getId().getTags())));
        buf.append(CHAR_SPACE);
        buf.append(page.getProperty(getTotalPropName()).getStringValue());
        return buf;
    }

    public static PrpcFunctionCounterBuilder builder() {
        return new PrpcFunctionCounterBuilder();
    }

    public static class PrpcFunctionCounterBuilder extends AbstractPrpcMeterBuilder<PrpcFunctionCounterBuilder> {

        private String totalPropName;

        private PrpcFunctionCounterBuilder() {
        }

        public PrpcFunctionCounterBuilder totalPropName(String totalPropName) {
            this.totalPropName = totalPropName;
            return self();
        }

        @Override
        protected PrpcFunctionCounterBuilder self() {
            return this;
        }

        @Override
        public PrpcFunctionCounter build() {
            return new PrpcFunctionCounter(this);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append("[").append(getId()).append("]");
        return sb.toString();
    }
}
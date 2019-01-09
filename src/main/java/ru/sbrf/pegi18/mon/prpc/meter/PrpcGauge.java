package ru.sbrf.pegi18.mon.prpc.meter;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import io.micrometer.core.instrument.Tags;

/**
 * @author Alexey Lapin
 */
public class PrpcGauge extends AbstractPrpcMeter {

    private String valuePropName;

    private PrpcGauge(BuilderPrpcGauge builder) {
        super(builder);
        this.valuePropName = builder.valuePropName;
    }

    public String getValuePropName() {
        return valuePropName;
    }

    @Override
    public String seriefy(ClipboardPage page) {
        StringBuilder buf = new StringBuilder();
        return seriefy(buf, page).toString();
    }

    @Override
    public StringBuilder seriefy(StringBuilder buf, ClipboardPage page) {
        long start = System.currentTimeMillis();
        namify(buf, getId());
        tagify(buf, Tags.of(propToTags(page.getIfPresent(getTagsPropName()))).and(getId().getTags()));
        buf.append(CHAR_SPACE);
        buf.append(page.getProperty(getValuePropName()).toDouble());
        if (logger.isDebugEnabled()) {
            logger.debug(toString() + " seriefy succeeded - spent: " + (System.currentTimeMillis() - start));
        }
        return buf;
    }

    public static BuilderPrpcGauge builder() {
        return new BuilderPrpcGauge();
    }

    public static class BuilderPrpcGauge extends AbstractPrpcMeterBuilder<BuilderPrpcGauge> {

        private String valuePropName;

        private BuilderPrpcGauge() {
        }

        public BuilderPrpcGauge valuePropName(String valuePropName) {
            this.valuePropName = valuePropName;
            return self();
        }

        @Override
        protected BuilderPrpcGauge self() {
            return this;
        }

        @Override
        public PrpcGauge build() {
            return new PrpcGauge(this);
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

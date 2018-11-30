package ru.sbrf.pegi18.mon.prpc;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import io.micrometer.core.instrument.Tags;

/**
 *
 */
public class PrpcGauge extends AbstractPrpcMeter {

    private String valuePropName;

    public String getValuePropName() {
        return valuePropName;
    }

    @Override
    public String seriefy(ClipboardPage page) {
        StringBuffer buf = new StringBuffer();
        return seriefy(buf, page).toString();
    }

    @Override
    public StringBuffer seriefy(StringBuffer buf, ClipboardPage page) {
        long start = System.currentTimeMillis();
        namify(buf, getId());
        tagify(buf, Tags.of(propToTags(page.getIfPresent(getTagsPropName()))).and(getId().getTags()));
        buf.append(" ");
        buf.append(page.getProperty(getValuePropName()).toDouble());
        if (logger.isDebugEnabled()) {
            logger.debug(toString() + " seriefy succeeded - spent: " + (System.currentTimeMillis() - start));
        }
        return buf;
    }

    public static Builder builder() {
        return new PrpcGauge().new Builder();
    }

    public class Builder extends AbstractPrpcMeter.Builder<Builder> {

        private String valuePropName;

        private Builder() {
        }

        public Builder valuePropName(String valuePropName) {
            this.valuePropName = valuePropName;
            return self();
        }

        public PrpcGauge build() {
            build(PrpcGauge.this);
            PrpcGauge.this.valuePropName = this.valuePropName;
            return PrpcGauge.this;
        }
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(getClass().getSimpleName());
        sb.append("[").append(getId()).append("]");
        return sb.toString();
    }
}

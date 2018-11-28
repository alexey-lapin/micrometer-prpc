package ru.sbrf.pegi18.mon.prpc;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import io.micrometer.core.instrument.Tags;

import java.io.StringWriter;

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
        StringWriter writer = new StringWriter();
        writer.append(namify(getId()));
        writer.append(tagify(Tags.of(propToTags(page.getIfPresent(getTagsPropName()))).and(getId().getTags())));
        writer.append(" ");
        writer.append(page.getProperty(getValuePropName()).getStringValue());

        return writer.toString();
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
}

package ru.sbrf.pegi18.mon.prpc;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import io.micrometer.core.instrument.Tags;

import java.io.StringWriter;

/**
 * @author lapin2-aa
 */
public class PrpcFunctionCounter extends AbstractPrpcMeter {

    private String totalPropName;

    public String getTotalPropName() {
        return totalPropName;
    }

    @Override
    public String seriefy(ClipboardPage page) {
        StringWriter writer = new StringWriter();
        writer.append(namify(getId()));
        writer.append(tagify(Tags.of(propToTags(page.getIfPresent(getTagsPropName()))).and(getId().getTags())));
        writer.append(" ");
        writer.append(page.getProperty(getTotalPropName()).getStringValue());

        return writer.toString();
    }

    @Override
    public StringBuffer seriefy(StringBuffer buf, ClipboardPage page) {
        return null;
    }

    public static PrpcFunctionCounter.Builder builder() {
        return new PrpcFunctionCounter().new Builder();
    }

    public class Builder extends AbstractPrpcMeter.Builder<PrpcFunctionCounter.Builder> {

        private String totalPropName;

        private Builder() {
        }

        public PrpcFunctionCounter.Builder totalPropName(String totalPropName) {
            this.totalPropName = totalPropName;
            return self();
        }

        public PrpcFunctionCounter build() {
            build(PrpcFunctionCounter.this);
            PrpcFunctionCounter.this.totalPropName = this.totalPropName;
            return PrpcFunctionCounter.this;
        }
    }
}

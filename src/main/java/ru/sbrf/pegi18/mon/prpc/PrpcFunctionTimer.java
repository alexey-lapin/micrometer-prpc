package ru.sbrf.pegi18.mon.prpc;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import io.micrometer.core.instrument.Tags;

import java.io.StringWriter;

/**
 * @author lapin2-aa
 */
public class PrpcFunctionTimer extends AbstractPrpcMeter {

    private String countValuePropName;
    private String sumValuePropName;

    public String getCountValuePropName() {
        return countValuePropName;
    }

    public String getSumValuePropName() {
        return sumValuePropName;
    }

    @Override
    public String seriefy(ClipboardPage page) {
        StringWriter writer = new StringWriter();
        writer.append(namify(getId())).append("_count");
        writer.append(tagify(Tags.of(propToTags(page.getIfPresent(getTagsPropName()))).and(getId().getTags())));
        writer.append(" ");
        writer.append(page.getProperty(getCountValuePropName()).getStringValue());

        writer.append("\n");

        writer.append(namify(getId())).append("_sum");
        writer.append(tagify(Tags.of(propToTags(page.getIfPresent(getTagsPropName()))).and(getId().getTags())));
        writer.append(" ");
        writer.append(page.getProperty(getSumValuePropName()).getStringValue());
        return writer.toString();
    }

    @Override
    public StringBuffer seriefy(StringBuffer buf, ClipboardPage page) {
        return null;
    }

    public static PrpcFunctionTimer.Builder builder() {
        return new PrpcFunctionTimer().new Builder();
    }

    public class Builder extends AbstractPrpcMeter.Builder<PrpcFunctionTimer.Builder> {

        private String countValuePropName;
        private String sumValuePropName;

        private Builder() {
        }

        public PrpcFunctionTimer.Builder countValuePropName(String countValuePropName) {
            this.countValuePropName = countValuePropName;
            return self();
        }

        public PrpcFunctionTimer.Builder sumValuePropName(String sumValuePropName) {
            this.sumValuePropName = sumValuePropName;
            return self();
        }

        public PrpcFunctionTimer build() {
            build(PrpcFunctionTimer.this);
            PrpcFunctionTimer.this.countValuePropName = this.countValuePropName;
            PrpcFunctionTimer.this.sumValuePropName = this.sumValuePropName;
            return PrpcFunctionTimer.this;
        }
    }
}

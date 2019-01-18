package ru.sbrf.pegi18.mon.prpc.meter;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import io.micrometer.core.instrument.Tags;

/**
 * @author Alexey Lapin
 */
public class PrpcFunctionTimer extends AbstractPrpcMeter {

    private static final String SUFFIX_COUNT = "_count";
    private static final String SUFFIX_SUM = "_sum";

    private final String countValuePropName;
    private final String sumValuePropName;

    private PrpcFunctionTimer(PrpcFunctionTimerBuilder builder) {
        super(builder);
        this.countValuePropName = builder.countValuePropName;
        this.sumValuePropName = builder.sumValuePropName;
    }

    public String getCountValuePropName() {
        return countValuePropName;
    }

    public String getSumValuePropName() {
        return sumValuePropName;
    }

    @Override
    public StringBuilder seriefy(StringBuilder buf, ClipboardPage page) {
        buf.append(namify(getId())).append(SUFFIX_COUNT);
        buf.append(tagify(Tags.of(propToTags(page.getIfPresent(getTagsPropName()))).and(getId().getTags())));
        buf.append(CHAR_SPACE);
        buf.append(page.getProperty(getCountValuePropName()).getStringValue());
        buf.append(CHAR_CARRIAGE_RETURN);
        buf.append(namify(getId())).append(SUFFIX_SUM);
        buf.append(tagify(Tags.of(propToTags(page.getIfPresent(getTagsPropName()))).and(getId().getTags())));
        buf.append(CHAR_SPACE);
        buf.append(page.getProperty(getSumValuePropName()).getStringValue());
        return buf;
    }

    public static PrpcFunctionTimerBuilder builder() {
        return new PrpcFunctionTimerBuilder();
    }

    public static class PrpcFunctionTimerBuilder extends AbstractPrpcMeterBuilder<PrpcFunctionTimerBuilder> {

        private String countValuePropName;
        private String sumValuePropName;

        private PrpcFunctionTimerBuilder() {
        }

        public PrpcFunctionTimerBuilder countValuePropName(String countValuePropName) {
            this.countValuePropName = countValuePropName;
            return self();
        }

        public PrpcFunctionTimerBuilder sumValuePropName(String sumValuePropName) {
            this.sumValuePropName = sumValuePropName;
            return self();
        }

        @Override
        protected PrpcFunctionTimerBuilder self() {
            return this;
        }

        @Override
        public PrpcFunctionTimer build() {
            return new PrpcFunctionTimer(this);
        }
    }
}
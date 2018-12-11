package ru.sbrf.pegi18.mon.prpc;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.prometheus.client.Collector;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 */
public abstract class AbstractPrpcMeter {

    public static final String PROP_NAME_TAG = "Tag";

    protected final Logger logger = LogManager.getLogger(getClass());

    private Meter.Id id;
    private MeterRegistry.Config config;

    private PrpcSource source;
    private String tagsPropName;

    public abstract String seriefy(ClipboardPage page);

    public abstract StringBuffer seriefy(StringBuffer buf, ClipboardPage page);

    public Meter.Id getId() {
        return id;
    }

    public MeterRegistry.Config getConfig() {
        return config;
    }

    public PrpcSource getSource() {
        return source;
    }

    public String getTagsPropName() {
        return tagsPropName;
    }

    @SuppressWarnings("unchecked")
    public String promify() {
        long start = System.currentTimeMillis();
        StringBuffer buf = new StringBuffer();

        try {
            StringBuffer meterBuf = new StringBuffer();
            ClipboardProperty results = getSource().collect();

            if (results != null && results.size() > 0) {
//            buf.append(header());
                header(meterBuf);

                results.iterator().forEachRemaining(r -> {
//                buf.append(seriefy(((ClipboardProperty) r).getPageValue())).append("\n");
                    seriefy(meterBuf, ((ClipboardProperty) r).getPageValue()).append("\n");
                });
            }
            // prevent partial meter appending
            buf.append(meterBuf);
            if (logger.isInfoEnabled()) {
                logger.info(toString() + " promify succeeded - spent: " + (System.currentTimeMillis() - start));
            }
        } catch (Exception ex) {
            logger.error(toString() + "promify failed", ex);
        }
        return buf.toString();
    }

    String header() {
        StringBuffer buf = new StringBuffer();
        return header(buf).toString();
    }

    StringBuffer header(StringBuffer buf) {
        buf.append("# HELP ").append(namify(getId())).append("\n");
        buf.append(("# TYPE ")).append(namify(getId())).append(" ").append(typify(getId())).append("\n");
        return buf;
    }

    protected String namify(Meter.Id meterId) {
        StringBuffer buf = new StringBuffer();
        return namify(buf, meterId).toString();
    }

    protected StringBuffer namify(StringBuffer buf, Meter.Id meterId) {
        buf.append(getConfig()
            .namingConvention()
            .name(meterId.getName(), meterId.getType(), meterId.getBaseUnit()));
        return buf;
    }

    protected String tagify(Iterable<Tag> tagIterable) {
        StringBuffer buf = new StringBuffer();
        return tagify(buf, tagIterable).toString();
    }

    protected StringBuffer tagify(StringBuffer buf, Iterable<Tag> tagIterable) {
        String tags = StreamSupport.stream(tagIterable.spliterator(), false)
            .map(t -> getConfig().namingConvention().tagKey(t.getKey()) + "=\"" + getConfig().namingConvention().tagValue(t.getValue()) + "\"")
            .collect(Collectors.joining(","));
        if (!StringUtils.isBlank(tags)) {
            buf.append("{").append(tags).append("}");
        }
        return buf;
    }

    protected String typify(Meter.Id meterId) {
        Collector.Type promType = Collector.Type.UNTYPED;
        switch (meterId.getType()) {
            case COUNTER:
                promType = Collector.Type.COUNTER;
                break;
            case GAUGE:
                promType = Collector.Type.GAUGE;
                break;
            case DISTRIBUTION_SUMMARY:
            case TIMER:
                promType = Collector.Type.SUMMARY;
        }
        return promType.toString().toLowerCase();
    }

    @SuppressWarnings("unchecked")
    Iterable<Tag> propToTags(ClipboardProperty property) {
        Tags tags = Tags.empty();
        if (property != null) {
            Iterator<ClipboardProperty> iter = (Iterator<ClipboardProperty>) property.iterator();
            while (iter.hasNext()) {
                ClipboardProperty e = iter.next();
                tags = tags.and(e.getName(), e.getStringValue());
            }
        }
        return tags;
    }

    public abstract static class Builder<T extends Builder<T>> {

        private Meter.Id id;
        private MeterRegistry.Config config;

        private PrpcSource source;
        private String tagsPropName = PROP_NAME_TAG;

        public T id(Meter.Id id) {
            this.id = id;
            return self();
        }

        public T config(MeterRegistry.Config config) {
            this.config = config;
            return self();
        }

        public T source(PrpcSource source) {
            this.source = source;
            return self();
        }

        public T tagsPropName(String tagsPropName) {
            this.tagsPropName = tagsPropName;
            return self();
        }

        @SuppressWarnings("unchecked")
        final T self() {
            return (T) this;
        }

        void build(AbstractPrpcMeter meter) {
            meter.id = this.id;
            meter.config = this.config;
            meter.source = this.source;
            meter.tagsPropName = this.tagsPropName;
        }
    }
}

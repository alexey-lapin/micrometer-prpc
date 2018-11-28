package ru.sbrf.pegi18.mon.prpc.meter;

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
import ru.sbrf.pegi18.mon.prpc.source.PrpcSource;

import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Alexey Lapin
 */
public abstract class AbstractPrpcMeter {

    private static final String PROM_HEADER_HELP = "# HELP ";
    private static final String PROM_HEADER_TYPE = "# TYPE ";

    protected static final char CHAR_CARRIAGE_RETURN = 10;
    protected static final char CHAR_SPACE = 32;

    public static final String PROP_NAME_TAG = "Tag";

    protected final Logger logger = LogManager.getLogger(getClass());

    private final Meter.Id id;
    private final MeterRegistry.Config config;
    private final PrpcSource source;
    private final String tagsPropName;

    protected AbstractPrpcMeter(AbstractPrpcMeterBuilder<?> builder) {
        this.id = builder.id;
        this.config = builder.config;
        this.source = builder.source;
        this.tagsPropName = builder.tagsPropName;
    }

    public abstract StringBuilder seriefy(StringBuilder buf, ClipboardPage page);

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
        StringBuilder buf = new StringBuilder();

        try {
            StringBuilder meterBuf = new StringBuilder();
            ClipboardProperty results = getSource().collect().orElse(null);

            if (results != null && results.size() > 0) {
                header(meterBuf);
                if (results.isList() || results.isGroup()) {
                    results.iterator().forEachRemaining(r -> seriefy(meterBuf, ((ClipboardProperty) r).getPageValue()).append(CHAR_CARRIAGE_RETURN));
                } else if (results.isPage()) {
                    seriefy(meterBuf, results.getPageValue()).append(CHAR_CARRIAGE_RETURN);
                }
            }
            // prevent partial meter appending
            buf.append(meterBuf);
            logger.info(() -> toString() + " promify succeeded - spent: " + (System.currentTimeMillis() - start));
        } catch (Exception ex) {
            logger.error(toString() + " promify failed", ex);
        }
        return buf.toString();
    }

    StringBuilder header(StringBuilder buf) {
        buf.append(PROM_HEADER_HELP).append(namify(getId())).append(CHAR_CARRIAGE_RETURN);
        buf.append(PROM_HEADER_TYPE).append(namify(getId())).append(CHAR_SPACE).append(typify(getId())).append(CHAR_CARRIAGE_RETURN);
        return buf;
    }

    protected String namify(Meter.Id meterId) {
        StringBuilder buf = new StringBuilder();
        return namify(buf, meterId).toString();
    }

    protected StringBuilder namify(StringBuilder buf, Meter.Id meterId) {
        buf.append(getConfig()
            .namingConvention()
            .name(meterId.getName(), meterId.getType(), meterId.getBaseUnit()));
        return buf;
    }

    protected String tagify(Iterable<Tag> tagIterable) {
        StringBuilder buf = new StringBuilder();
        return tagify(buf, tagIterable).toString();
    }

    protected StringBuilder tagify(StringBuilder buf, Iterable<Tag> tagIterable) {
        String tags = StreamSupport.stream(tagIterable.spliterator(), false)
            .map(t -> getConfig().namingConvention().tagKey(t.getKey()) + "=\"" + getConfig().namingConvention().tagValue(t.getValue()) + "\"")
            .collect(Collectors.joining(","));
        if (!StringUtils.isBlank(tags)) {
            buf.append("{").append(tags).append("}");
        }
        return buf;
    }

    protected String typify(Meter.Id meterId) {
        Collector.Type promType = null;
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
                break;
            default:
                promType = Collector.Type.UNTYPED;
                break;
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

    /**
     * Fluent builder for prpc meters
     *
     * @param <T> hierarchical builder support
     */
    public abstract static class AbstractPrpcMeterBuilder<T extends AbstractPrpcMeterBuilder<T>> {

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

        /**
         * @return actual builder
         */
        protected abstract T self();

        /**
         * @return ready to use prpc meter object
         */
        public abstract AbstractPrpcMeter build();
    }
}
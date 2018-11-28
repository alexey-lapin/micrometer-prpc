package ru.sbrf.pegi18.mon.prpc;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 */
public abstract class AbstractPrpcMeter {

    private Meter.Id id;
    private MeterRegistry.Config config;

    private PrpcSource source;
    private String tagsPropName;

    public abstract String seriefy(ClipboardPage page);

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
        StringBuffer buf = new StringBuffer();

        ClipboardProperty results = getSource().collect();

        buf.append(header());

        results.iterator().forEachRemaining(r -> {
            buf.append(seriefy(((ClipboardProperty) r).getPageValue())).append("\n");
        });
        return buf.toString();
    }

    String header() {
        Writer writer = new StringWriter();
        try {
            writer.append("# HELP ").append(namify(getId())).append("\n");
            writer.append(("# TYPE ")).append(namify(getId())).append("\n");
        } catch (IOException ex) {

        }
        return writer.toString();
    }

    protected String namify(Meter.Id meterId) {
        return getConfig()
            .namingConvention()
            .name(meterId.getName(), meterId.getType(), meterId.getBaseUnit());
    }

    protected String tagify(Iterable<Tag> tagIterable) {
        String tags = StreamSupport.stream(tagIterable.spliterator(), false)
            .map(t -> getConfig().namingConvention().tagKey(t.getKey()) + "=\"" + getConfig().namingConvention().tagValue(t.getValue()) + "\"")
            .collect(Collectors.joining(","));
        return "{" + tags + "}";
    }

    @SuppressWarnings("unchecked")
    Iterable<Tag> propToTags(ClipboardProperty property) {
        Tags tags = Tags.empty();
        if (property != null) {
            ((Iterator<ClipboardProperty>) property.iterator()).forEachRemaining(e -> tags.and(e.getName(), e.getStringValue()));
        }
        return tags;
    }

    public abstract static class Builder<T extends Builder<T>> {

        private Meter.Id id;
        private MeterRegistry.Config config;

        private PrpcSource source;
        private String tagsPropName = "Tag";

//        private Builder() {
//        }

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

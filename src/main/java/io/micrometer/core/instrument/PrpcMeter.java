package io.micrometer.core.instrument;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import io.micrometer.core.instrument.prpc.PrpcSource;
import org.elasticsearch.Build;

/**
 * Created by sp00x on 25-Nov-18.
 * Project: pg-micrometer
 */
public abstract class PrpcMeter {

    private Meter.Id id;
    private PrpcSource source;
    private String tagsPropName;

    public PrpcMeter(Meter.Id id, PrpcSource source) {
        this(id, source, "Tag");
    }

    public PrpcMeter(Meter.Id id, PrpcSource source, String tagsPropName) {
        this.id = id;
        this.source = source;
        this.tagsPropName = tagsPropName;
    }

//    public abstract String promify();
    public abstract String seriefy(ClipboardPage page);

    public Meter.Id getId() {
        return id;
    }

    public PrpcSource getSource() {
        return source;
    }

    public String getTagsPropName() {
        return tagsPropName;
    }

    @SuppressWarnings("unchecked")
    public String promify() {
        ClipboardProperty results = getSource().collect();

        results.iterator().forEachRemaining(r -> {
            seriefy(((ClipboardProperty)r).getPageValue());
        });
        return null;
    }

    Iterable<Tag> propToTags(ClipboardProperty property) {
        return null;
    }

    public static Builder builder(Meter.Id id) {
        return new Builder(id);

    }

    static class Builder {

        private Meter.Id id;

        protected Builder(Meter.Id id) {
            this.id = id;
        }

    }
}

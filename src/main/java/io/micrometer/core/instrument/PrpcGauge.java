package io.micrometer.core.instrument;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import io.micrometer.core.instrument.prpc.PrpcSource;

/**
 * Created by sp00x on 25-Nov-18.
 * Project: pg-micrometer
 */
public class PrpcGauge extends PrpcMeter {

    private String valuePropName;

    private PrpcGauge() {
        this(null, null, null, null);
    }

    public PrpcGauge(Meter.Id id, PrpcSource source, String tagsPropName, String valuePropName) {
        super(id, source, tagsPropName);
        this.valuePropName = valuePropName;
    }


    public String getValuePropName() {
        return valuePropName;
    }

    @Override
    public String seriefy(ClipboardPage page) {
        Iterable<Tag> rowTags = propToTags(page.getIfPresent(getTagsPropName())); // tags
        Tags.empty().and(getId().getTags()).and(rowTags); // all tags
        page.getProperty(getValuePropName()).getStringValue(); // value
        //name{tags} value
        return null;
    }

    public static Builder builder() {



        return new PrpcGauge().new Builder(null);
    }

    class Builder extends PrpcMeter.Builder {

        private Builder(Meter.Id id) {
            super(id);
        }

    }
}

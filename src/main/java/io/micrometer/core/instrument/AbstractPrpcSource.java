package io.micrometer.core.instrument;

import com.pega.pegarules.priv.PegaAPI;
import com.pega.pegarules.pub.context.ThreadContainer;
import io.micrometer.core.instrument.prpc.PrpcSource;

/**
 * Created by sp00x on 24-Nov-18.
 * Project: pg-micrometer
 */
abstract class AbstractPrpcSource implements PrpcSource {

    private Meter.Id meterId;
//    private MeterRegistry registry;

    private PegaAPI tools;

    private String accessGroup;
    private String ruleName;
    private String ruleClass;

    PegaAPI tools() {
        if (tools == null) {
            tools = (PegaAPI) ThreadContainer.get().getPublicAPI();
        }
        return tools;
    }

    @Override
    public Meter.Id id() {
        return meterId;
    }

    @Override
    public PrpcSource id(Meter.Id meterId) {
        this.meterId = meterId;
        return this;
    }

    @Override
    public String valueProp() {
        return null;
    }

    @Override
    public String tagsProp() {
        return null;
    }

    public String accessGroup() {
        return accessGroup;
    }

    public String ruleName() {
        return ruleName;
    }

    public String ruleClass() {
        return ruleClass;
    }
}

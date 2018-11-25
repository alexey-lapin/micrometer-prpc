package io.micrometer.core.instrument.prpc;

import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import io.micrometer.core.instrument.Meter;

/**
 * Created by sp00x on 15-Nov-18.
 * Project: pg-micrometer
 */
public interface PrpcSource {

    Meter.Id id();

    PrpcSource id(Meter.Id meterId);

    String valueProp();

    String tagsProp();

    ClipboardProperty collect();



}

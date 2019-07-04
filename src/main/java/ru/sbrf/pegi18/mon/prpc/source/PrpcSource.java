package ru.sbrf.pegi18.mon.prpc.source;

import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import ru.sbrf.pegi18.mon.prpc.meter.AbstractPrpcMeter;

import java.util.Optional;

/**
 * An object that represents a way of obtaining data.
 * Serves as a data source for callback-based meters using prpc.
 *
 * @author Alexey Lapin
 * @see AbstractPrpcMeter
 */
public interface PrpcSource {


    Optional<ClipboardProperty> get();

    /**
     * @return {@code Optional ClipboardProperty} that contains results of requested data
     */
    Optional<ClipboardProperty> collect();

}
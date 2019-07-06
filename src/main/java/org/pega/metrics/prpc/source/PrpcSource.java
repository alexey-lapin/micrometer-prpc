package org.pega.metrics.prpc.source;

import com.pega.pegarules.pub.clipboard.ClipboardProperty;

import java.util.Optional;

/**
 * An object that represents a way of obtaining data.
 * Serves as a data source for callback-based meters having an ability to use prpc entities.
 *
 * @author Alexey Lapin
 */
public interface PrpcSource {

    /**
     * @return {@code Optional ClipboardProperty} that contains results of requested data
     */
    Optional<ClipboardProperty> get();

}
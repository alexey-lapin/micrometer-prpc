package ru.sbrf.pegi18.mon.prpc.source;

import com.pega.pegarules.priv.PegaAPI;
import com.pega.pegarules.pub.context.ThreadContainer;
import com.pega.pegarules.pub.runtime.ParameterPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Skeletal implementation of {@code PrpcSource} interface
 *
 * @author Alexey Lapin
 */
public abstract class AbstractPrpcSource implements PrpcSource {

    protected final Logger logger = LogManager.getLogger(getClass());

    public static final Supplier<PegaAPI> DEFAULT_PEGAAPI_SUPPLIER = () -> (PegaAPI) ThreadContainer.get().getPublicAPI();

    private final Supplier<? extends PegaAPI> toolsSupplier;
    private final ParameterPage parameterPage;

    protected AbstractPrpcSource(AbstractPrpcSourceBuilder<?> builder) {
        this.toolsSupplier = builder.toolsSupplier;
        this.parameterPage = builder.parameterPage;
    }

    public ParameterPage parameterPage() {
        return parameterPage;
    }

    PegaAPI tools() {
        return toolsSupplier.get();
    }

    /**
     * Fluent builder for prpc sources
     *
     * @param <T> hierarchical builder support
     */
    public abstract static class AbstractPrpcSourceBuilder<T extends AbstractPrpcSourceBuilder<T>> {
        private Supplier<? extends PegaAPI> toolsSupplier = DEFAULT_PEGAAPI_SUPPLIER;
        private ParameterPage parameterPage;

        public T parameterPage(ParameterPage parameterPage) {
            Objects.requireNonNull(parameterPage);
            this.parameterPage = new ParameterPage();
            this.parameterPage.putAll(parameterPage);
            return self();
        }

        public T toolsSupplier(Supplier<? extends PegaAPI> toolsSupplier) {
            this.toolsSupplier = Objects.requireNonNull(toolsSupplier);
            return self();
        }

        /**
         * @return actual builder
         */
        protected abstract T self();

        /**
         * @return ready to use prpc source object
         */
        public abstract AbstractPrpcSource build();
    }
}
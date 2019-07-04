package ru.sbrf.pegi18.mon.prpc.source;

import com.google.common.base.Suppliers;
import com.pega.pegarules.priv.PegaAPI;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import com.pega.pegarules.pub.context.ThreadContainer;
import com.pega.pegarules.pub.runtime.ParameterPage;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Skeletal implementation of {@code PrpcSource} interface
 *
 * @author Alexey Lapin
 */
public abstract class AbstractPrpcSource implements PrpcSource {

    @SuppressWarnings("WeakerAccess")
    protected final Logger logger = LogManager.getLogger(getClass());

    public static final Supplier<PegaAPI> DEFAULT_PEGAAPI_SUPPLIER = () -> (PegaAPI) ThreadContainer.get().getPublicAPI();

    private final Supplier<? extends PegaAPI> toolsSupplier;
    private final Supplier<Optional<ClipboardProperty>> valueSupplier;
    private final ParameterPage parameterPage;
    private final boolean isGroupResult;

    protected AbstractPrpcSource(AbstractPrpcSourceBuilder<?> builder) {
        this.toolsSupplier = builder.toolsSupplier;
        this.parameterPage = builder.parameterPage;
        this.isGroupResult = builder.isGroupResult;
        if (builder.expirationDuration > 0 && builder.expirationTimeUnit != null) {
            // Using Guava supplier
            valueSupplier = Suppliers.memoizeWithExpiration(this::collect, builder.expirationDuration, builder.expirationTimeUnit)::get;
        } else {
            valueSupplier = this::collect;
        }
    }

    public ParameterPage parameterPage() {
        return parameterPage;
    }
    public boolean isGroupResult() {
        return isGroupResult;
    }

    PegaAPI tools() {
        return toolsSupplier.get();
    }

    @Override
    public Optional<ClipboardProperty> get() {
        return valueSupplier.get();
    }

    /**
     * Fluent builder for prpc sources
     *
     * @param <T> hierarchical builder support
     */
    public abstract static class AbstractPrpcSourceBuilder<T extends AbstractPrpcSourceBuilder<T>> {

        private Supplier<? extends PegaAPI> toolsSupplier = DEFAULT_PEGAAPI_SUPPLIER;
        private ParameterPage parameterPage;
        private long expirationDuration = 0;
        private TimeUnit expirationTimeUnit = TimeUnit.MINUTES;
        private boolean isGroupResult;

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

        public T expirationDuration(long expirationDuration) {
            this.expirationDuration = expirationDuration;
            return self();
        }

        public T expirationTimeUnit(TimeUnit expirationTimeUnit) {
            this.expirationTimeUnit = expirationTimeUnit;
            return self();
        }

        public T groupResults() {
            this.isGroupResult = true;
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

        protected PrpcSource cached(Function<? super Integer, ? extends PrpcSource> mappingFunction) {
            return SourceManager.getInstance().getCache().asMap().computeIfAbsent(hashCode(), mappingFunction);
        }

        @Override
        public int hashCode() {
//            return new HashCodeBuilder()
//                .append(getClass().getName())
//                .append(parameterPage)
//                .hashCode();
            return computeHash(parameterPage);
        }

        @Override
        public boolean equals(Object other) {
            if (other == null) return false;
            if (other == this) return true;
            if (other.getClass() != getClass()) return false;

            AbstractPrpcSourceBuilder builder = (AbstractPrpcSourceBuilder) other;
            return new EqualsBuilder()
                .append(parameterPage, builder.parameterPage)
                .isEquals();
        }
    }

    private static int computeHash(Object... fields) {
        return new HashCodeBuilder()
            .append(fields)
            .hashCode();
    }

    @Override
    public int hashCode() {
        return computeHash(parameterPage);
    }
}
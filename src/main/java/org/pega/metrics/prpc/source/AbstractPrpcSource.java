package org.pega.metrics.prpc.source;

import com.google.common.base.Suppliers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.pega.pegarules.priv.PegaAPI;
import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import com.pega.pegarules.pub.context.ThreadContainer;
import com.pega.pegarules.pub.runtime.ParameterPage;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pega.metrics.prpc.PrpcTags;

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
    private final String tagsPropName;
    private final String groupPropName;

    protected AbstractPrpcSource(AbstractPrpcSourceBuilder<?> builder) {
        this.toolsSupplier = builder.toolsSupplier;
        this.parameterPage = builder.parameterPage;
        this.tagsPropName = builder.tagsPropName;
        this.groupPropName = builder.groupPropName;
        if (builder.expirationDuration > 0) {
            valueSupplier = Suppliers.memoizeWithExpiration(this::collect, builder.expirationDuration, builder.expirationTimeUnit)::get;
        } else {
            valueSupplier = this::collect;
        }
    }

    public ParameterPage parameterPage() {
        return parameterPage;
    }

    public String tagsPropName() {
        return tagsPropName;
    }

    public String groupPropName() {
        return groupPropName;
    }

    PegaAPI tools() {
        return toolsSupplier.get();
    }

    @Override
    public Optional<ClipboardProperty> get() {
        return valueSupplier.get();
    }

    /**
     * @return {@code Optional ClipboardProperty} that contains results of requested data
     */
    abstract protected Optional<ClipboardProperty> collect();

    @SuppressWarnings("unchecked")
    protected ClipboardProperty groupResults(ClipboardProperty sourceProp, ClipboardProperty targetProp) {
        if (sourceProp != null && (sourceProp.isList() || sourceProp.isGroup()) && !sourceProp.isEmpty()) {
            for (ClipboardProperty item : (Iterable<ClipboardProperty>) sourceProp) {
                targetProp.add(PrpcTags.id(item.getProperty(tagsPropName())), item);
            }
            return targetProp;
        }
        return null;
    }

    protected void finalize(ClipboardPage page) {
        if (page != null) {
            try {
                page.removeFromClipboard();
            } catch (Exception ex) {
                logger.error(toString() + " failed to clean up", ex);
            }
        }
    }

    /**
     * Fluent builder for prpc sources
     *
     * @param <T> hierarchical builder support
     */
    public abstract static class AbstractPrpcSourceBuilder<T extends AbstractPrpcSourceBuilder<T>> {
        private static Cache<AbstractPrpcSourceBuilder, PrpcSource> cache = CacheBuilder.newBuilder().weakValues().build();

        private Supplier<? extends PegaAPI> toolsSupplier = DEFAULT_PEGAAPI_SUPPLIER;
        private ParameterPage parameterPage;
        private String tagsPropName = "Tag";
        private String groupPropName;
        private long expirationDuration = 0;
        private TimeUnit expirationTimeUnit = TimeUnit.SECONDS;

        public T parameterPage(ParameterPage parameterPage) {
            this.parameterPage = new ParameterPage();
            this.parameterPage.putAll(Objects.requireNonNull(parameterPage));
            return self();
        }

        public T tagsPropName(String tagsPropName) {
            this.tagsPropName = Objects.requireNonNull(tagsPropName);
            return self();
        }

        public T groupPropName(String groupPropName) {
            this.groupPropName = Objects.requireNonNull(groupPropName);
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
            this.expirationTimeUnit = Objects.requireNonNull(expirationTimeUnit);
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

        protected PrpcSource cached(Function<? super AbstractPrpcSourceBuilder, ? extends PrpcSource> mappingFunction) {
            return cache.asMap().computeIfAbsent(this, mappingFunction);
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder()
                .append(getClass().getName())
                .append(parameterPage)
                .append(tagsPropName)
                .append(groupPropName)
                .hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (other == null) return false;
            if (other == this) return true;
            if (other.getClass() != getClass()) return false;

            AbstractPrpcSourceBuilder builder = (AbstractPrpcSourceBuilder) other;
            return new EqualsBuilder()
                .append(parameterPage, builder.parameterPage)
                .append(tagsPropName, builder.tagsPropName)
                .append(groupPropName, builder.groupPropName)
                .isEquals();
        }
    }
}
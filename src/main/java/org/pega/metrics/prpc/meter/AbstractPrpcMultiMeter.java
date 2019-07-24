package org.pega.metrics.prpc.meter;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.pega.metrics.prpc.source.PrpcSource;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.StreamSupport;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

/**
 * Skeletal implementation of multi-meters
 *
 * @see io.micrometer.core.instrument.MultiGauge
 * @author Alexey Lapin
 */
public abstract class AbstractPrpcMultiMeter {

    private final AtomicReference<Set<Meter.Id>> registeredRows = new AtomicReference<>(emptySet());

    private final MeterRegistry registry;
    private final Meter.Id commonId;
    private final PrpcSource source;

    AbstractPrpcMultiMeter(AbstractPrpcMultiMeterBuilder<?> builder) {
        this.registry = builder.registry;
        this.source = builder.source;
        this.commonId = new Meter.Id(builder.name, builder.tags, builder.baseUnit, builder.description, builder.type);
    }

    MeterRegistry getRegistry() {
        return registry;
    }

    PrpcSource getSource() {
        return source;
    }

    public void register() {
        register(false);
    }

    /**
     * Get new rows from source and re-register meters
     * @param overwrite already registered meters
     */
    public void register(boolean overwrite) {
        registeredRows.getAndUpdate(oldRows -> {
            Set<Meter.Id> newRows = StreamSupport.stream(rows(source).spliterator(), false)
                    .map(row -> {
                        Meter.Id rowId = commonId.withTags(row.tags);
                        boolean previouslyDefined = oldRows.contains(rowId);

                        if (overwrite && previouslyDefined) {
                            registry.remove(rowId);
                        }

                        if (overwrite || !previouslyDefined) {
                            registerMeter(rowId, row);
                        }

                        return rowId;
                    }).collect(toSet());

            for (Meter.Id oldRow : oldRows) {
                if (!newRows.contains(oldRow))
                    registry.remove(oldRow);
            }

            return newRows;
        });
    }

    protected abstract Iterable<AbstractRow> rows(PrpcSource source);

    protected abstract void registerMeter(Meter.Id rowId, AbstractRow row);

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getClass().getName())
                .append(commonId)
                .hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (other.getClass() != getClass()) return false;

        AbstractPrpcMultiMeter meter = (AbstractPrpcMultiMeter) other;
        return new EqualsBuilder()
                .append(commonId, meter.commonId)
                .isEquals();
    }

    protected static class AbstractRow {
        private final Tags tags;

        AbstractRow(Tags tags) {
            this.tags = tags;
        }

        public Tags getTags() {
            return tags;
        }
    }

    /**
     * Fluent builder for multi-meters.
     */
    public static abstract class AbstractPrpcMultiMeterBuilder<T extends AbstractPrpcMultiMeterBuilder<T>> {
        private MeterRegistry registry;
        private PrpcSource source;
        private String name;
        private String baseUnit;
        private String description;
        private Meter.Type type;
        private Tags tags = Tags.empty();

        /**
         * @return this builder
         */
        protected abstract T self();

        /**
         * @param name Meter name
         * @return The meter builder with added name.
         */
        T name(String name) {
            this.name = name;
            return self();
        }

        /**
         * @param type Meter type
         * @return The meter builder with added type.
         */
        T type(Meter.Type type) {
            this.type = type;
            return self();
        }

        /**
         * @param tags Must be an even number of arguments representing key/value pairs of tags.
         * @return The meter builder with added tags.
         */
        public T tags(String... tags) {
            return tags(Tags.of(tags));
        }

        /**
         * @param tags Tags to add to the eventual gauge.
         * @return The meter builder with added tags.
         */
        public T tags(Iterable<Tag> tags) {
            this.tags = this.tags.and(tags);
            return self();
        }

        /**
         * @param key   The tag key.
         * @param value The tag value.
         * @return The meter builder with a single added tag.
         */
        public T tag(String key, String value) {
            this.tags = tags.and(key, value);
            return self();
        }

        /**
         * @param description Description text of the eventual gauge.
         * @return The meter builder with added description.
         */
        public T description(String description) {
            this.description = description;
            return self();
        }

        /**
         * @param unit Base unit of the eventual gauge.
         * @return The meter builder with added base unit.
         */
        public T baseUnit(String unit) {
            this.baseUnit = unit;
            return self();
        }

        /**
         * @param registry Registry where register meters to
         * @return The meter builder with added registry.
         */
        public T registry(MeterRegistry registry) {
            this.registry = registry;
            return self();
        }

        /**
         * @param source Source from where get tags and values for meters
         * @return The meter builder with added source.
         */
        public T source(PrpcSource source) {
            this.source = source;
            return self();
        }

        /**
         * @return Ready to use multi meter
         */
        public abstract AbstractPrpcMultiMeter build();
    }
}

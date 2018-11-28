package ru.sbrf.pegi18.mon.prpc.source;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * A {@code PrpcSource} implementation which uses Data Page as underlying source of data
 *
 * @author Alexey Lapin
 */
public class DataPageSource extends AbstractRulePrpcSource {

    DataPageSource(AbstractRulePrpcSourceBuilder builder) {
        super(builder);
    }

    @Override
    protected Optional<ClipboardProperty> obtain() {
        ClipboardProperty resultsProp = null;
        try {
            ClipboardPage page = tools().findPage(ruleName(), parameterPage());
            Objects.requireNonNull(page, "Failed to find data page " + ruleName());
            if (StringUtils.isBlank(resultsPropName())) {
                resultsProp = wrap(page);
            } else {
                resultsProp = page.getProperty(resultsPropName());
            }
        } catch (Exception ex) {
            logger.error(toString() + " obtain failed", ex);
        }
        return Optional.ofNullable(resultsProp);
    }

    public static DataPageSourceBuilder builder() {
        return new DataPageSourceBuilder();
    }

    public static class DataPageSourceBuilder extends AbstractRulePrpcSourceBuilder<DataPageSourceBuilder> {

        private DataPageSourceBuilder() {
        }

        @Override
        protected DataPageSourceBuilder self() {
            return this;
        }

        @Override
        public DataPageSource build() {
            return (DataPageSource) cached(k -> new DataPageSource(this));
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append("[").append(ruleName()).append("]");
        sb.append("[").append(accessGroupName()).append("]");
        sb.append("[").append(resultsPropName()).append("]");
        return sb.toString();
    }
}

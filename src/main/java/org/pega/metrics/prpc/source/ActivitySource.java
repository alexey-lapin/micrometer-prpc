package org.pega.metrics.prpc.source;

import com.pega.pegarules.priv.factory.ThreadLocalStringMapFactoryImpl;
import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import com.pega.pegarules.pub.util.StringMap;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * A {@code PrpcSource} implementation which uses Activity as data provider
 *
 * @author Alexey Lapin
 */
public class ActivitySource extends AbstractRulePrpcSource {

    ActivitySource(ActivitySourceBuilder builder) {
        super(builder);
    }

    @Override
    protected Optional<ClipboardProperty> obtain() {
        ClipboardProperty resultsProp = null;
        ClipboardPage primaryPage = null;
        try {
            primaryPage = tools().createPage(ruleClass(), null);
//            StringMap keys = ThreadLocalStringMapFactoryImpl.getFactory().acquire();
            StringMap keys = null;
            tools().doActivity(keys, primaryPage, parameterPage());
//            tools().invokeActivity(primaryPage, parameterPage(), ruleName(), ruleClass(), "");
            if (StringUtils.isNotBlank(resultsPropName())) {
                if (StringUtils.isNotBlank(groupPropName())) {
                    resultsProp = groupResults(primaryPage.getIfPresent(resultsPropName()), primaryPage.getProperty(groupPropName()));
                } else {
                    resultsProp = primaryPage.getProperty(resultsPropName());
                }
            } else {
                resultsProp = wrap(primaryPage);
            }
        } catch (Exception ex) {
            logger.error(toString() + " obtain failed", ex);
        } finally {
            finalize(primaryPage);
        }
        return Optional.ofNullable(resultsProp);
    }

    public static ActivitySourceBuilder builder() {
        return new ActivitySourceBuilder();
    }

    public static class ActivitySourceBuilder extends AbstractRulePrpcSourceBuilder<ActivitySourceBuilder> {

        private ActivitySourceBuilder() {
        }

        @Override
        protected ActivitySourceBuilder self() {
            return this;
        }

        @Override
        public ActivitySource build() {
            return (ActivitySource) cached(k -> new ActivitySource(this));
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append("[").append(ruleClass()).append("]");
        sb.append("[").append(ruleName()).append("]");
        sb.append("[").append(accessGroupName()).append("]");
        sb.append("[").append(resultsPropName()).append("]");
        return sb.toString();
    }
}

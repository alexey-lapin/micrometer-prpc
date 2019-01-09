package ru.sbrf.pegi18.mon.prpc.source;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;

import java.util.Optional;

/**
 * A {@code PrpcSource} implementation which uses Activity as data provider
 *
 * @author Alexey Lapin
 */
public class ActivitySource extends AbstractRulePrpcSource {

    private ActivitySource(ActivitySourceBuilder builder) {
        super(builder);
    }

    @Override
    protected Optional<ClipboardProperty> obtain() {
        ClipboardProperty resultsProp = null;
        ClipboardPage primaryPage = null;
        try {
            primaryPage = tools().createPage(ruleClass(), null);
            tools().invokeActivity(primaryPage, parameterPage(), ruleName(), ruleClass(), "");
            resultsProp = primaryPage.getProperty(resultsPropName());
        } catch (Exception ex) {
            logger.error(toString() + " obtain failed", ex);
        } finally {
            if (primaryPage != null) {
                try {
                    primaryPage.removeFromClipboard();
                } catch (Exception ex) {
                    logger.error(toString() + " failed to clean up", ex);
                }
            }
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
            return new ActivitySource(this);
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

package org.pega.metrics.prpc.source;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import com.pega.pegarules.pub.util.StringMap;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class ActionRuleSource extends AbstractRulePrpcSource {

    public ActionRuleSource(AbstractRulePrpcSourceBuilder<?> builder) {
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
            tools().doAction(keys, primaryPage, parameterPage());
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
}

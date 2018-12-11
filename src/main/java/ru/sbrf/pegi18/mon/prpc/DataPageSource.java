package ru.sbrf.pegi18.mon.prpc;

import com.pega.pegarules.pub.PRRuntimeException;
import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class DataPageSource extends AbstractPrpcSource {

    @Override
    public ClipboardProperty collect() {

        long start = System.currentTimeMillis();
        if (logger.isDebugEnabled()) {
            logger.debug(toString() + " collect using executable " + tools());
        }

        String currentAccessGroup = getCurrentAccessGroup();
        if (!StringUtils.isBlank(currentAccessGroup) && !currentAccessGroup.equals(accessGroup())) {
            switchAccessGroup(accessGroup());
        }

        ClipboardProperty prop = null;
        try {
            ClipboardPage page = tools().findPage(ruleName(), parameterPage());
            if (page == null) {
                throw new PRRuntimeException("Failed to find data page " + ruleName());
            }
            prop = page.getProperty(resultsPropName());
            int size = 0;
            if (prop != null) {
                size = prop.size();
            }
            logger.info(toString() + " collect succeeded - size: " + size + " spent: " + (System.currentTimeMillis() - start));
        } catch (Exception ex) {
            logger.error(toString() + " collect failed", ex);
        } finally {
            if (!StringUtils.isBlank(currentAccessGroup) && !currentAccessGroup.equals(accessGroup())) {
                switchAccessGroup(currentAccessGroup);
            }
        }
        return prop;
    }

    public static DataPageSource.Builder builder() {
        return new DataPageSource().new Builder();
    }

    public class Builder extends AbstractPrpcSource.Builder<Builder> {

        private Builder() {
        }

        public DataPageSource build() {
            build(DataPageSource.this);
            return DataPageSource.this;
        }
    }
}

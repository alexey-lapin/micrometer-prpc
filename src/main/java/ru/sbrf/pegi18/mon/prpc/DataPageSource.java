package ru.sbrf.pegi18.mon.prpc;

import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class DataPageSource extends AbstractPrpcSource {

    @Override
    public ClipboardProperty collect() {

        // switch ag
        String currentAccessGroup = getCurrenAccessGroup();
        switchAccessGroup(accessGroup());

        // find datapage
        ClipboardProperty prop = null;
        try {
            prop = tools().findPage(ruleName()).getProperty(resultsPropName());
        } catch (Exception ex) {
//            oLog.in
        } finally {
            // switch back
            if (!StringUtils.isBlank(currentAccessGroup)) {
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

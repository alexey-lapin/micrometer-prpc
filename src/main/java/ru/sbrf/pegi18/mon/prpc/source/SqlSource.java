package ru.sbrf.pegi18.mon.prpc.source;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;

import java.util.Optional;

/**
 * A {@code PrpcSource} implementation which uses sql query directly as data provider
 *
 * @author Alexey Lapin
 */
public class SqlSource extends AbstractPrpcSource {

    private static final String CLASS_CODE_PEGA_LIST = "Code-Pega-List";

    private static final String PROP_SQLSOURCEBROWSEPAGE = "SqlSourceBrowsePage";
    private static final String PROP_PXRESULTS = "pxResults";
    private static final String PROP_PYMAXRECORDS = "pyMaxRecords";

    private final String queryString;
    private final int maxRecords;

    SqlSource(SqlSourceBuilder builder) {
        super(builder);
        this.queryString = builder.queryString;
        this.maxRecords = builder.maxRecords;
    }

    public String queryString() {
        return queryString;
    }

    public int maxRecords() {
        return maxRecords;
    }

    @Override
    public Optional<ClipboardProperty> collect() {
        ClipboardProperty resultsProp = null;
        ClipboardPage browsePage = null;
        try {
            browsePage = tools().createPage(CLASS_CODE_PEGA_LIST, PROP_SQLSOURCEBROWSEPAGE);
            browsePage.putString(PROP_PYMAXRECORDS, String.valueOf(maxRecords()));
            // TODO: use params
            tools().getDatabase().executeRDB(queryString(), browsePage);
            resultsProp = browsePage.getProperty(PROP_PXRESULTS);
        } catch (Exception ex) {
            logger.error("Failed to collect", ex);
        } finally {
            if (browsePage != null) {
                try {
                    browsePage.removeFromClipboard();
                } catch (Exception ex) {
                    logger.error(toString() + " failed to clean up", ex);
                }
            }
        }
        return Optional.ofNullable(resultsProp);
    }

    public static SqlSourceBuilder builder() {
        return new SqlSourceBuilder();
    }

    public static class SqlSourceBuilder extends AbstractPrpcSourceBuilder<SqlSourceBuilder> {
        private String queryString;
        private int maxRecords = 10000;

        private SqlSourceBuilder() {
        }

        public SqlSourceBuilder queryString(String queryString) {
            this.queryString = queryString;
            return self();
        }

        public SqlSourceBuilder maxRecords(int maxRecords) {
            this.maxRecords = maxRecords;
            return self();
        }

        @Override
        protected SqlSourceBuilder self() {
            return this;
        }

        @Override
        public SqlSource build() {
            return new SqlSource(this);
        }
    }
}


package org.jumpmind.symmetric.jdbc.db;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.jumpmind.symmetric.core.db.AbstractPlatform;
import org.jumpmind.symmetric.core.model.Parameters;
import org.jumpmind.symmetric.core.model.Table;

abstract public class AbstractJdbcPlatform extends AbstractPlatform {

    protected DataSource dataSource;

    protected JdbcModelReader jdbcModelReader;

    public Table findTable(String catalogName, String schemaName, String tableName,
            boolean useCache, Parameters parameters) {
        Table cachedTable = cachedModel.findTable(catalogName, schemaName, tableName);
        if (cachedTable == null || !useCache) {
            Table justReadTable = jdbcModelReader.readTable(catalogName, schemaName, tableName,
                    parameters.is(Parameters.DB_METADATA_IGNORE_CASE, true),
                    parameters.is(Parameters.DB_USE_ALL_COLUMNS_AS_PK_IF_NONE_FOUND, false));
            if (cachedTable != null) {
                cachedModel.removeTable(cachedTable);
            }

            if (justReadTable != null) {
                cachedModel.addTable(justReadTable);
            }

            cachedTable = justReadTable;
        }
        return cachedTable;
    }

    public java.util.List<Table> findTables(String catalog, String schema, Parameters parameters) {
        return null;
    };

    protected void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public boolean isDataIntegrityException(Exception ex) {
        boolean integrityError = false;
        if (ex instanceof SQLException) {
            int sqlErrorCode = ((SQLException) ex).getErrorCode();
            int[] codes = getDataIntegritySqlErrorCodes();
            for (int i : codes) {
                if (sqlErrorCode == i) {
                    integrityError = true;
                    break;
                }
            }
        }
        return integrityError;
    }

    abstract protected int[] getDataIntegritySqlErrorCodes();

}

package io.hypersistence.utils.hibernate.util.providers;

import org.hibernate.dialect.PostgreSQLDialect;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

/**
 * @author Vlad Mihalcea
 */
public class PostgreSQLDataSourceProvider extends AbstractContainerDataSourceProvider {

    @Override
    public String hibernateDialect() {
        return PostgreSQLDialect.class.getName();
    }

    @Override
    protected String defaultJdbcUrl() {
        return "jdbc:postgresql://localhost/high_performance_java_persistence";
    }

    protected DataSource newDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL(url());
        dataSource.setUser(username());
        dataSource.setPassword(password());

        return dataSource;
    }

    @Override
    public String username() {
        return "postgres";
    }

    @Override
    public String password() {
        return "admin";
    }

    @Override
    public Database database() {
        return Database.POSTGRESQL;
    }
}

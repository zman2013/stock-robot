package com.zman.stock;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/**
 * 未被使用
 * 
 * @author zman
 *
 */
@Configuration
@MapperScan("com.zman.stock.data.dao")
public class MybatisConfig {

    @Value("${stock.db.url}")
    private String stockDBUrl;
    @Value("${stock.db.username}")
    private String stockDBUsername;
    @Value("${stock.db.password}")
    private String stockDBPassword;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public DataSource getDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl(stockDBUrl);
        dataSource.setUsername(stockDBUsername);
        dataSource.setPassword(stockDBPassword);
        return dataSource;
    }

    @Bean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(getDataSource());
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(getDataSource());
        sessionFactory.setMapperLocations(applicationContext
                .getResources("classpath:mybatis/*.xml"));
        return sessionFactory.getObject();
    }
}

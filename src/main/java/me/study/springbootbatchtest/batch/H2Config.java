package me.study.springbootbatchtest.batch;

import org.h2.tools.Server;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class H2Config {
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() throws SQLException {
//        Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9099").start();
        Server.createTcpServer("-tcp", "-tcpAllowOthers").start();
        return new org.apache.tomcat.jdbc.pool.DataSource();
    }

//    @Bean
//    public Server h2TcpServer() throws SQLException {
//        return Server.createTcpServer().start();
//    }
}

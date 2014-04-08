package me.dhallam.springdataneo4jdemo.config;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableNeo4jRepositories(
        basePackages = "me.dhallam.springdataneo4jdemo")
public class Neo4jTestConfig extends Neo4jConfiguration {

    public Neo4jTestConfig() {
        // http://forum.spring.io/forum/spring-projects/data/nosql/746297-spring-data-neo4j-fails-on-upgrade-to-3-0-1
        setBasePackage("me.dhallam.springdataneo4jdemo");
    }
    
    @Bean(
            destroyMethod = "shutdown")
    public GraphDatabaseService graphDatabaseService() {
        return new TestGraphDatabaseFactory().newImpermanentDatabase();
    }

}
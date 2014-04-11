package me.dhallam.springdataneo4jdemo.config;

import static me.dhallam.springdataneo4jdemo.Application.BASE_PACKAGE;
import me.dhallam.springdataneo4jdemo.Application;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.WrappingNeoServerBootstrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.aspects.config.Neo4jAspectConfiguration;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;

@Configuration
@EnableNeo4jRepositories(basePackageClasses = Application.class)
@SuppressWarnings("deprecation")
public class Neo4jConfig extends Neo4jAspectConfiguration {

	public Neo4jConfig() {
		setBasePackage(BASE_PACKAGE);
	}

	@Bean(destroyMethod = "shutdown")
	public GraphDatabaseService graphDatabaseService() {
		return new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder("target/neo4jdb")
				.setConfig(GraphDatabaseSettings.allow_store_upgrade, "true")
				.newGraphDatabase();
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	public WrappingNeoServerBootstrapper neo4jWebServer() {
		return new WrappingNeoServerBootstrapper(
				(GraphDatabaseAPI) graphDatabaseService());
	}
}

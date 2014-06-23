package me.dhallam.springdataneo4jdemo.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class DeadlocksViaEmbeddedTest {
	@Test
	public void testWithEngine() throws Exception {

		GraphDatabaseService db = new GraphDatabaseFactory()
				.newEmbeddedDatabase("/tmp/thomson-reuters");

		final ExecutionEngine engine = new ExecutionEngine(db);

		final HashMap<String, Object> params = new HashMap<>();
		params.put("value", "aaa");
		final HashMap<String, Object> props = new HashMap<>();
		props.put("idCode", "aaa");
		params.put("props", props);

		engine.execute("CREATE CONSTRAINT ON (n:`Person`) ASSERT n.`id` IS UNIQUE");
		engine.execute(
				"MERGE (n:`Person` {`idCode`: {value}}) ON CREATE SET n={props} return n",
				params);

		ExecutorService executorService = Executors.newFixedThreadPool(50);

		List<Future<?>> jobs = new ArrayList<>();

		for (int i = 0; i < 500; i++) {
			Future<?> job = executorService.submit(new Runnable() {
				@Override
				public void run() {
					ExecutionResult result = engine
							.execute(
									"MERGE (n:`Person` {`idCode`: {value}}) ON CREATE SET n={props} return n",
									params);
					String name = Thread.currentThread().getName();

					System.out.println(name + "\n" + result.dumpToString());
				}
			});
			jobs.add(job);

		}

		for (Future<?> future : jobs) {
			future.get();
		}

		executorService.shutdown();
		db.shutdown();
	}
}

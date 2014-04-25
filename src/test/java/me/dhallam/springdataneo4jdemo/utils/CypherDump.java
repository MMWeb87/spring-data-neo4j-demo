package me.dhallam.springdataneo4jdemo.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import org.junit.Test;
import org.neo4j.cypher.export.DatabaseSubGraph;
import org.neo4j.cypher.export.SubGraphExporter;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Utility to dump the cypher statements that would allow the database to be
 * recreated, e.g. using the Neo4j browser. Handy for debugging models and
 * queries.
 */
public class CypherDump {

	public static void dump(GraphDatabaseService db) {
		StackTraceElement[] traces = new Exception().fillInStackTrace()
				.getStackTrace();

		for (StackTraceElement trace : traces) {
			String className = trace.getClassName();
			String methodName = trace.getMethodName();
			try {
				Method m = Class.forName(className).getMethod(methodName);
				if (m.isAnnotationPresent(Test.class)) {
					dump(db, m);
					return;
				}
			} catch (Exception e) {
				// Ignore - means we haven't yet hit the test method as maybe
				// this method has params that we don't know about
			}
		}

	}

	private static void dump(GraphDatabaseService db, Method m) {
		File f = new File("target/graph-cql/"
				+ m.getDeclaringClass().getCanonicalName().replace('.', '/')
				+ "-" + m.getName() + ".cql");
		f.getParentFile().mkdirs();

		try (PrintWriter w = new PrintWriter(new FileWriter(f))) {
			new SubGraphExporter(DatabaseSubGraph.from(db)).export(w);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

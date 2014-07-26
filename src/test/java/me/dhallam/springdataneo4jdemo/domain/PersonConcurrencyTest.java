package me.dhallam.springdataneo4jdemo.domain;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.transaction.Status;

import me.dhallam.springdataneo4jdemo.config.Neo4jTestConfig;
import me.dhallam.springdataneo4jdemo.domain.Person.Gender;
import me.dhallam.springdataneo4jdemo.utils.CypherDump;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.jta.JtaTransactionManager;

/**
 * The classes in the @ContextConfiguration are the implementations that need to
 * be injected into the test context.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Neo4jTestConfig.class }, loader = AnnotationConfigContextLoader.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class PersonConcurrencyTest {

	private static final Logger LOG = LoggerFactory
			.getLogger(PersonConcurrencyTest.class);

	@Autowired
	protected GraphDatabaseService database;

	@Autowired
	protected PersonRepository personRepo;

	@Configuration
	@ComponentScan(basePackages = { "me.dhallam.springdataneo4jdemo" })
	@Profile("test")
	static class TestContextConfiguration {

	}

	/** To double check the transaction status at various points. */
	@Autowired
	JtaTransactionManager txManager;

	@Autowired
	Neo4jTemplate template;

	@Autowired
	ExecutionEngine engine;

	@Test
	public void firstThreadCreatesNodeAheadOfOthersEngine() throws Exception {
		firstThreadCreatesNodeAheadOfOthers(true);
	}

	/**
	 * TODO Pending https://jira.spring.io/browse/DATAGRAPH-484
	 * 
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void firstThreadCreatesNodeAheadOfOthersNodeEntity()
			throws Exception {
		firstThreadCreatesNodeAheadOfOthers(false);
	}

	/**
	 * All threads run in parallel, but the first thread has a clear head start
	 * to call persist. This is currently resulting in most of the following
	 * transactions deadlocking. Contrast to the
	 * threadsCreateSameNodeInParallel() test which is the same with the
	 * exception of the head start. But each transaction starts with the same
	 * view of the database.
	 */
	private void firstThreadCreatesNodeAheadOfOthers(boolean useEngine)
			throws Exception {
		assertThat(txManager.getUserTransaction().getStatus(),
				equalTo(Status.STATUS_NO_TRANSACTION));

		String idCode = "aaa";

		ExecutorService executorService = Executors.newFixedThreadPool(10);

		List<Throwable> exceptions = Collections
				.synchronizedList(new ArrayList<Throwable>());

		// First thread - tx waits an addition 5000ms so that the following
		// threads all start their tx's before the first has committed (i.e. an
		// up-front query on the DB from the following threads will not find the
		// node if it queried
		executorService.execute(new TransactionalNodeCreator("t1", 0, 1000,
				idCode, exceptions, useEngine));
		for (int i = 2; i <= 50; i++) {
			// Delay start of trying to persist by 1000ms so that the first
			// thread has chance to do it and these should queue up behind, in
			// theory. When these threads create their tx, the node from t1
			// will not have been committed.
			executorService.execute(new TransactionalNodeCreator("t" + i, 1000,
					100, idCode, exceptions, useEngine));
		}

		executorService.shutdown();
		executorService.awaitTermination(100, TimeUnit.MINUTES);

		for (Throwable e : exceptions) {
			String msg = e.getMessage();
			LOG.error(e.getClass() + " - "
					+ msg.substring(msg.indexOf("\nDetails: ") + 9));
			e.printStackTrace();
		}
		assertThat(exceptions.size(), equalTo(0));
	}

	@Test
	public void threadsCreateNodesInSeriesEngine() throws Exception {
		threadsCreateNodesInSeries(true);
	}

	@Test
	public void threadsCreateNodesInSeriesNodeEntity() throws Exception {
		threadsCreateNodesInSeries(false);
	}

	/**
	 * Create an initial Person with a fixed idCode, then invoke the test that
	 * creates the same node over multiple threads.
	 */
	private void threadsCreateNodesInSeries(boolean useEngine) throws Exception {
		assertThat(txManager.getUserTransaction().getStatus(),
				equalTo(Status.STATUS_NO_TRANSACTION));

		String idCode = "aaa";

		final List<Throwable> exceptions = new ArrayList<>();

		for (int i = 1; i <= 10; i++) {
			// Spawn a separate thread for the creator for completeness
			Thread t = new Thread(new TransactionalNodeCreator("t" + i, 1000,
					100, idCode, exceptions, useEngine));
			t.start();
			t.join();
		}

		assertThat(exceptions.size(), equalTo(0));
		assertThat(personRepo.count(), equalTo(1L));
	}

	@Test
	public void threadsCreateNodesInParallelWhenNodeAlreadyExistsEngine()
			throws Exception {
		threadsCreateNodesInParallelWhenNodeAlreadyExists(true);
	}

	/**
	 * TODO Pending https://jira.spring.io/browse/DATAGRAPH-484
	 * 
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void threadsCreateNodesInParallelWhenNodeAlreadyExistsNodeEntity()
			throws Exception {
		threadsCreateNodesInParallelWhenNodeAlreadyExists(false);
	}

	/**
	 * Create an initial Person with a fixed idCode, then invoke the test that
	 * creates the same node over multiple threads.
	 */
	private void threadsCreateNodesInParallelWhenNodeAlreadyExists(
			boolean useEngine) throws Exception {
		assertThat(txManager.getUserTransaction().getStatus(),
				equalTo(Status.STATUS_NO_TRANSACTION));

		String idCode = "aaa";
		String name = "pre";
		final List<Throwable> exceptions = new ArrayList<>();

		// Spawn a separate thread for the creator for completeness
		Thread t = new Thread(new TransactionalNodeCreator(name, 1000, 100,
				idCode, exceptions, useEngine));
		t.start();
		t.join();

		assertThat(exceptions.size(), equalTo(0));
		assertThat(personRepo.count(), equalTo(1L));

		// Now try to create the same nodes in parallel
		threadsCreateSameNodeInParallel(useEngine);
	}

	@Test
	public void threadsCreateSameNodeInParallelEngine() throws Exception {
		threadsCreateSameNodeInParallel(true);
	}

	/**
	 * TODO Pending https://jira.spring.io/browse/DATAGRAPH-484
	 * 
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void threadsCreateSameNodeInParallelNodeEntity() throws Exception {
		threadsCreateSameNodeInParallel(false);
	}

	/**
	 * Create a Person node by invoking a number of simultaneous threads, each
	 * creating its own transaction. SDN executes
	 * <code>MERGE (n:`Person` {`idCode`: {value}}) ON CREATE SET n={props} return n</code>
	 * . The first thread to persist() gets a lock and the other threads block
	 * until that tx commits. Then another thread picks up the lock, persists,
	 * commits, then the next thread, etc.
	 */
	private void threadsCreateSameNodeInParallel(boolean useEngine)
			throws Exception {
		assertThat(txManager.getUserTransaction().getStatus(),
				equalTo(Status.STATUS_NO_TRANSACTION));

		String idCode = "aaa";
		int threadCount = 10;

		ExecutorService executorService = Executors
				.newFixedThreadPool(threadCount);

		List<Throwable> exceptions = Collections
				.synchronizedList(new ArrayList<Throwable>());

		for (int i = 1; i <= threadCount; i++) {
			executorService.execute(new TransactionalNodeCreator("t" + i, 100,
					500, idCode, exceptions, useEngine));
		}

		executorService.shutdown();
		executorService.awaitTermination(1, TimeUnit.MINUTES);

		for (Throwable e : exceptions) {
			String msg = e.getMessage();
			LOG.error(e.getClass() + " - "
					+ msg.substring(msg.indexOf("\nDetails: ") + 9));
		}
		assertThat(exceptions.size(), equalTo(0));

		assertThat(personRepo.count(), equalTo(1L));
	}

	/**
	 * Simple utility class to create a Person within a transaction.
	 */
	class TransactionalNodeCreator implements Runnable {
		final long initialSleep;
		final long finalSleep;
		final String idCode;
		final List<Throwable> exceptions;
		final String name;
		final boolean useEngine;

		public TransactionalNodeCreator(String name, long initialSleep,
				long finalSleep, String idCode, List<Throwable> exceptions,
				boolean useEngine) {
			this.name = name;
			this.initialSleep = initialSleep;
			this.finalSleep = finalSleep;
			this.idCode = idCode;
			this.exceptions = exceptions;
			this.useEngine = useEngine;
		}

		public TransactionalNodeCreator(String name, long initialSleep,
				long finalSleep, String idCode, List<Throwable> exceptions) {
			this(name, initialSleep, finalSleep, idCode, exceptions, false);
		}

		@Override
		public void run() {
			LOG.debug("{} starting", name);
			try (Transaction tx = database.beginTx()) {
				assertThat(txManager.getUserTransaction().getStatus(),
						equalTo(Status.STATUS_ACTIVE));
				LOG.debug("{} going to sleep for {}ms", name, initialSleep);
				Thread.sleep(initialSleep);
				LOG.debug("{} awake", name);
				LOG.debug("{} about to persist", name);

				if (useEngine) {
					final HashMap<String, Object> params = new HashMap<>();
					params.put("value", "aaa");
					final HashMap<String, Object> props = new HashMap<>();
					props.put("idCode", "aaa");
					params.put("props", props);
					engine.execute(
							"MERGE (n:`Person` {`idCode`: {value}}) ON CREATE SET n={props} return n",
							params);
				} else {
					new Person().setIdCode(idCode).persist();
				}
				LOG.debug("{} persisted", name);
				LOG.debug("{} going to sleep for {}ms", name, finalSleep);
				Thread.sleep(finalSleep);
				LOG.debug("{} awake", name);
				tx.success();
				LOG.debug("{} success", name);
			} catch (Exception e) {
				exceptions.add(e);
				LOG.debug("{} failed", name);
			}
			LOG.debug("{} exiting", name);
		}
	}

	@Test
	public void concurrentCreation() throws Exception {
		final String idCode = "theid";
		Runnable thread1 = new Runnable() {
			public void run() {
				try (Transaction tx = database.beginTx()) {
					Thread.sleep(500);
					assertThat(txManager.getUserTransaction().getStatus(),
							equalTo(Status.STATUS_ACTIVE));

					LOG.info("Creating Bob");
					Person bob = new Person().setIdCode(idCode)
							.setFirstName("Bob").setLastName("Smith")
							.setGender(Gender.MALE);
					// Thread.sleep(700); // Delay here makes the output Bob
					// Smith
					bob.persist();
					Person mary = new Person().setIdCode("maryId")
							.setFirstName("Mary").setLastName("Stevenson")
							.persist();
					mary.addFriends(bob);
					Thread.sleep(2000);
					tx.success();
					LOG.info("Created Bob");
				} catch (Exception e) {
					LOG.error("Failed");
				}
			}
		};
		Runnable thread2 = new Runnable() {
			public void run() {
				try (Transaction tx = database.beginTx()) {
					Thread.sleep(1000);
					assertThat(txManager.getUserTransaction().getStatus(),
							equalTo(Status.STATUS_ACTIVE));
					LOG.info("Creating Pete");
					Person pete = new Person().setIdCode(idCode)
							.setFirstName("Pete").setLastName("Walker");
					Thread.sleep(700);
					pete.persist();
					tx.success();
					Thread.sleep(2000);
					LOG.info("Created Pete");
				} catch (Exception e) {
					LOG.error("Failed");
				}
			}
		};

		ExecutorService executorService = Executors.newFixedThreadPool(2);

		executorService.execute(thread1);
		executorService.execute(thread2);

		executorService.shutdown();
		executorService.awaitTermination(1, TimeUnit.MINUTES);
		try (Transaction tx = database.beginTx()) {
			assertThat(personRepo.count(), equalTo(2L));
			Person person = personRepo.findByIdCode(idCode);
			LOG.info("{}", person);
			CypherDump.dump(database);
			tx.success();
		}
	}
}

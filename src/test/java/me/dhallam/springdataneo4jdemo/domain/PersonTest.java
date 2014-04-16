package me.dhallam.springdataneo4jdemo.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import me.dhallam.springdataneo4jdemo.config.Neo4jTestConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Neo4jTestConfig.class }, loader = AnnotationConfigContextLoader.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class PersonTest {

	private static final Logger LOG = LoggerFactory.getLogger(PersonTest.class);

	@Autowired
	private PersonRepository personRepo;

	@Autowired
	private GraphDatabaseService database;

	@Test
	public void idStillVisibleOutsideImplicitTx() {
		Person p = new Person();
		p = p.persist(); // call to persist will create a tx
		LOG.info("p.getId(): " + p.getId());
	}

	@Test
	public void idStillVisibleOutsideExlicitTx() {
		Person p = new Person();
		// Explicitly create tx and commit
		try (Transaction tx = database.beginTx()) {
			p.persist();
			assertThat(p.getId(), equalTo(0L));
			assertThat(p.getNodeId(), equalTo(0L));
			tx.success();
		}

		assertThat(p.getId(), equalTo(0L));
		assertThat(p.getNodeId(), equalTo(0L));
	}

	@Test
	public void inTxOutTxInTx() {

		Person p = new Person();
		
		assertThat(p.getId(), nullValue());

		try (Transaction tx = database.beginTx()) {
			assertThat(personRepo.count(), equalTo(0L));

			assertThat(p.getId(), nullValue());

			p.setFirstName("MyFN1");
			p.setLastName("MyLN1");
			assertThat(p.getFirstName(), equalTo("MyFN1"));
			assertThat(p.getLastName(), equalTo("MyLN1"));
			p.persist();

			LOG.info("p1.getId(): " + p.getId());
			LOG.info("p1.getNodeId(): " + p.getNodeId());

			assertThat(personRepo.count(), equalTo(1L));

			tx.success();
		}

		LOG.info("Outside tx");
		assertThat(p.getId(), equalTo(0L));
		assertThat(p.getNodeId(), equalTo(0L));

		try (Transaction tx = database.beginTx()) {
			LOG.info("Started new transaction");

			assertThat(personRepo.count(), equalTo(1L));

			assertThat(p.getId(), equalTo(0L));
			assertThat(p.getNodeId(), equalTo(0L));

			tx.success();
		}

	}
}

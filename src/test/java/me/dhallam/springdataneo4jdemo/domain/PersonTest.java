package me.dhallam.springdataneo4jdemo.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
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
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Neo4jTestConfig.class }, loader = AnnotationConfigContextLoader.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class PersonTest {

	private static final Logger LOG = LoggerFactory.getLogger(PersonTest.class);

	@Autowired
	private PersonRepository personRepo;

	@Autowired
	private GraphDatabaseService database;

	/**
	 * Simple starter test.
	 */
	@Test
	public void createPerson() {

		try (Transaction tx = database.beginTx()) {
			assertThat(personRepo.count(), equalTo(0L));

			Person p1 = new Person();
			p1.setFirstName("MyFN1");
			p1.setLastName("MyLN1");
			assertThat(p1.getFirstName(), equalTo("MyFN1"));
			assertThat(p1.getLastName(), equalTo("MyLN1"));
			p1.persist();

			LOG.info("p1.getId(): " + p1.getId());
			LOG.info("p1.getNodeId(): " + p1.getNodeId());

			assertThat(personRepo.count(), equalTo(1L));

			Person p2 = new Person();
			p2.setFirstName("MyFN2");
			p2.setLastName("MyLN2");
			assertThat(p2.getFirstName(), equalTo("MyFN2"));
			assertThat(p2.getLastName(), equalTo("MyLN2"));
			p2.persist();

			LOG.info("p2.getId(): " + p2.getId());
			LOG.info("p2.getNodeId(): " + p2.getNodeId());

			assertThat(personRepo.count(), equalTo(2L));

			Person p3 = new Person();
			p3.setFirstName("MyFN3");
			p3.setLastName("MyLN3");
			assertThat(p3.getFirstName(), equalTo("MyFN3"));
			assertThat(p3.getLastName(), equalTo("MyLN3"));
			p3.persist();

			LOG.info("p3.getId(): " + p3.getId());
			LOG.info("p3.getNodeId(): " + p3.getNodeId());

			assertThat(personRepo.count(), equalTo(3L));
			
			tx.success();
		}

	}
}

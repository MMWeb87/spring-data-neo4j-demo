package me.dhallam.springdataneo4jdemo.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import me.dhallam.springdataneo4jdemo.config.Neo4jTestConfig;
import me.dhallam.springdataneo4jdemo.domain.Person.Gender;
import me.dhallam.springdataneo4jdemo.utils.CypherDump;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.IteratorUtil;
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

	@Test
	@Transactional
	public void testRepository() {
		assertThat(personRepo.count(), equalTo(0L));

		Person john = new Person().setIdCode("A").setGender(Gender.MALE)
				.setFirstName("John").setLastName("Smith").persist();
		Person jane = new Person().setIdCode("B").setGender(Gender.FEMALE)
				.setFirstName("Jane").setLastName("Smith").persist();
		Person pete = new Person().setIdCode("C").setGender(Gender.MALE)
				.setFirstName("Pete").setLastName("Marsden").persist();

		pete.addFriends(john, jane);

		CypherDump.dump(database);

		assertThat(personRepo.count(), equalTo(3L));

		// MATCH (`person`:`Person`) WHERE `person`.`firstName` = {0} AND
		// `person`.`lastName` = {1} RETURN `person`
		assertThat(
				IteratorUtil.asList(
						personRepo.findByFirstNameAndLastName("John", "Smith"))
						.size(), equalTo(1));

		// MATCH (`person`:`Person`) WHERE `person`.`lastName` = {0} RETURN
		// `person`
		assertThat(IteratorUtil.asList(personRepo.findByLastName("Smith"))
				.size(), equalTo(2));

		// MATCH (`person`:`Person`) WHERE `person`.`gender` = {0} RETURN
		// `person`
		assertThat(IteratorUtil.asList(personRepo.findByGender(Gender.MALE))
				.size(), equalTo(2));

		// MATCH (`person`:`Person`) WHERE `person`.`gender` = {0} RETURN
		// `person`
		assertThat(IteratorUtil.asList(personRepo.findByGender(Gender.FEMALE))
				.size(), equalTo(1));

		// MATCH (`person`)-[:`FRIENDS_WITH`]-(`person_friends`) WHERE
		// `person_friends`.`gender` = {0} RETURN `person`
		// Should return Pete as he is friends with Jane
		assertThat(
				IteratorUtil.asList(
						personRepo.findByFriendsGender(Gender.FEMALE)).size(),
				equalTo(1));

		// MATCH (`person`)-[:`FRIENDS_WITH`]-(`person_friends`),
		// (`person`)-[:`FRIENDS_WITH`]-(`person_friends`) WHERE
		// `person_friends`.`lastName` = 'Smith' AND `person_friends`.`gender` =
		// 'MALE'
		// RETURN `person`
		// Should return Pete as he is friends with John Smith
		assertThat(
				IteratorUtil.asList(
						personRepo.findByFriendsLastNameAndFriendsGender(
								"Smith", Gender.MALE)).size(), equalTo(1));
	}
}

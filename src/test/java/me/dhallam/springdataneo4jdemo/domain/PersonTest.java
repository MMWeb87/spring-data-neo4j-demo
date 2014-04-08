package me.dhallam.springdataneo4jdemo.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import me.dhallam.springdataneo4jdemo.config.Neo4jTestConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = { Neo4jTestConfig.class }, loader = AnnotationConfigContextLoader.class)
@DirtiesContext(
        classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class PersonTest {

	/**
	 * Simple starter test.
	 */
	@Test
	public void createPerson() {
		Person p = new Person();
		p.setFirstName("MyFN");
		p.setLastName("MyLN");
		assertThat(p.getFirstName(), equalTo("MyFN"));
		assertThat(p.getLastName(), equalTo("MyLN"));
		p.persist();
	}
}

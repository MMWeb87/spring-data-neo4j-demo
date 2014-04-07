package me.dhallam.springdataneo4jdemo.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public class PersonTest {

	@Test
	public void createPerson() {
		Person p = new Person();
		p.setFirstName("MyFN");
		p.setLastName("MyLN");
		assertThat(p.getFirstName(), equalTo("MyFN"));
		assertThat(p.getLastName(), equalTo("MyLN"));
	}
}

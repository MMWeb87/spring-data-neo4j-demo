package me.dhallam.springdataneo4jdemo.domain;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.neo4j.graphdb.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.AccessType;
import org.springframework.data.annotation.AccessType.Type;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

@NodeEntity
public class Person {

	private static final Logger LOG = LoggerFactory.getLogger(Person.class);

	enum Gender {
		MALE, FEMALE
	}

	@GraphId
	@AccessType(Type.PROPERTY)
	private Long id;

	public Long getId() {
		// getNodeId() doesn't exist until it's weaved in, so the initial mvn compilation
		// fails if it's directly coded here, so do it reflectively.
		try {
			Method m = this.getClass().getMethod("getNodeId");
			return (Long)m.invoke(this);
		} catch (Exception e) {
			throw new RuntimeException("Failed to invoke getNodeId()");
		} 
	}

	@RelatedTo(type = "FRIENDS_WITH", direction = Direction.BOTH)
	private Set<Person> friends;

	private String idCode;
	private String firstName;
	private String lastName;
	private Gender gender;

	private transient Integer hash;

	public Person addFriends(Person... newFriends) {
		for (Person p : newFriends) {
			LOG.info("Adding " + p);
			if (!friends.contains(p)) {
				friends.add(p);
			}
			LOG.info("Friends now: " + friends);
		}
		return this;
	}

	public Set<Person> getFriends() {
		return Collections.unmodifiableSet(friends);
	}

	public String getIdCode() {
		return idCode;
	}

	public Person setIdCode(String idCode) {
		this.idCode = idCode;
		return this;
	}

	public String getFirstName() {
		return firstName;
	}

	public Person setFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	public String getLastName() {
		return lastName;
	}

	public Person setLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}

	public Gender getGender() {
		return gender;
	}

	public Person setGender(Gender gender) {
		this.gender = gender;
		return this;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public boolean equals(Object other) {
		LOG.info(this + " equals " + other);
		
		if (this == other) {
			return true;
		}

		if (idCode == null) {
			return false;
		}

		if (!(other instanceof Person)) {
			return false;
		}

		
		return getIdCode().equals(((Person) other).getIdCode());
	}

	@Override
	public int hashCode() {
		if (hash == null) {
			hash = idCode == null ? System.identityHashCode(this) : idCode
					.hashCode();
		}
		return hash.hashCode();
	}
}

package me.dhallam.springdataneo4jdemo.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.AccessType;
import org.springframework.data.annotation.AccessType.Type;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.fieldaccess.DynamicProperties;

@NodeEntity
public class Person {

	private static final Logger LOG = LoggerFactory.getLogger(Person.class);

	enum Gender {
		MALE, FEMALE
	}

	@GraphId
	@AccessType(Type.PROPERTY)
	private Long id;

	@RelatedTo(type = "FRIENDS_WITH", direction = Direction.BOTH)
	private Set<Person> friends;

	@Indexed(unique = true)
	private String idCode;

	private String firstName;
	private String lastName;
	private Gender gender;
	private Set<String> phoneNumbers = new HashSet<>();
	private DynamicProperties additionalMetadata;

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

	public Set<String> getPhoneNumbers() {
		return Collections.unmodifiableSet(phoneNumbers);
	}

	public Person addPhoneNumber(String phoneNumber) {
		this.phoneNumbers.add(phoneNumber);
		return this;
	}

	public Person removePhoneNumber(String phoneNumber) {
		this.phoneNumbers.remove(phoneNumber);
		return this;
	}

	public Map<String, Object> getAdditionalMetadata() {
		return Collections.unmodifiableMap(additionalMetadata.asMap());
	}

	public Person addAdditionalMetadata(String key, String value) {
		additionalMetadata.setProperty(key, value);
		return this;
	}

	public Person removeAdditionalMetadata(String key) {
		additionalMetadata.removeProperty(key);
		return this;
	}

	@Override
	public String toString() {
		return "Person [id=" + id + ", friends.size()=" + friends.size() + ", "
				+ "idCode=" + idCode + ", firstName=" + firstName
				+ ", lastName=" + lastName + ", gender=" + gender
				+ ", phoneNumbers=" + phoneNumbers + ", additionalMetadata="
				+ additionalMetadata + "]";
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

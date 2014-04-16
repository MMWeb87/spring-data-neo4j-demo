package me.dhallam.springdataneo4jdemo.domain;

import org.springframework.data.annotation.AccessType;
import org.springframework.data.annotation.AccessType.Type;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class Person {
	@GraphId
	@AccessType(Type.PROPERTY)
	private Long id;
	
	public Long getId() {
		return getNodeId();
	}

	private String firstName;
	private String lastName;
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}

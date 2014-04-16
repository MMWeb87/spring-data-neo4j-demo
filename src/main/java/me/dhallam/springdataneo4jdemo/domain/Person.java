package me.dhallam.springdataneo4jdemo.domain;

import java.lang.reflect.Method;

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
		// getNodeId() doesn't exist until it's weaved in, so the initial mvn compilation
		// fails if it's directly coded here, so do it reflectively.
		try {
			Method m = this.getClass().getMethod("getNodeId");
			return (Long)m.invoke(this);
		} catch (Exception e) {
			throw new RuntimeException("Failed to invoke getNodeId()");
		} 
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

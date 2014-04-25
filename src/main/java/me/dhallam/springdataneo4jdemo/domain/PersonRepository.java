package me.dhallam.springdataneo4jdemo.domain;

import me.dhallam.springdataneo4jdemo.domain.Person.Gender;

import org.springframework.data.neo4j.conversion.Result;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "people", path = "people")
public interface PersonRepository extends GraphRepository<Person> {

	Result<Person> findByFirstNameAndLastName(String firstName, String lastName);
	
	Result<Person> findByLastName(String lastName);
	
	Result<Person> findByGender(Gender gender);
	
	Result<Person> findByFriendsLastName(String lastName);
	
	Result<Person> findByFriendsGender(Gender gender);
	
	Result<Person> findByFriendsLastNameAndFriendsGender(String lastName, Gender gender);
}

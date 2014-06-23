package me.dhallam.springdataneo4jdemo.utils;

import java.util.Map;

import javax.transaction.Transaction;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility Aspect class to intercept method calls, etc. within the code,
 * including 3rd party libraries NB: for third party libraries, add a
 * weaveDependency to the aspectj-maven-plugin in the pom.
 */
@Aspect
public class DebugAspect {
	private static final Logger LOG = LoggerFactory
			.getLogger(DebugAspect.class);

	@Before("execution(* org.springframework.data.neo4j.support.query.CypherQueryEngineImpl.query(..)) && args(query, params)")
	public void CypherQueryEngineImpl_query(String query,
			Map<String, Object> params) {
		LOG.info("{}, {}", query, params);
	}

	@Before("execution(* org.neo4j.kernel.impl.transaction.LockManagerImpl.getReadLock(..)) && args(resource, tx)")
	public void LockManager_getReadLock(Object resource, Transaction tx) {
		LOG.info("{}, {}", resource, tx);
	}

	@Before("execution(* org.neo4j.kernel.impl.transaction.LockManagerImpl.getWriteLock(..)) && args(resource, tx)")
	public void LockManager_getWriteLock(Object resource, Transaction tx) {
		LOG.info("{}, {}", resource, tx);
	}

	@Before("execution(* org.springframework.data.neo4j.fieldaccess.DetachedEntityState.persist())")
	public void DetachedEntityState_persist() {
		LOG.info("no params");
	}

}
package me.dhallam.springdataneo4jdemo.utils;

import java.lang.reflect.Field;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.data.neo4j.fieldaccess.DetachedEntityState;
import org.springframework.data.neo4j.mapping.Neo4jPersistentProperty;

/**
 * This is an aspect to explore how to fix DeadlockDetectedExceptions on
 * IndexEntryLocks within SDN due to MERGEs taking a READ lock, followed by
 * setting the unique property and trying to acquire a WRITE lock on the
 * IndexEntryLock when concurrent transactions are trying to merge the same
 * nodes.
 * 
 * This is a pure hack to avoid having to checkout the latest SDN code, build,
 * etc.
 */
@Aspect
public class IndexNodeLockDeadlockFixAspect {

	@Pointcut("execution(* org.springframework.data.neo4j.fieldaccess.DetachedEntityState.flushDirty(..))")
	public void intercept() {
	}

	@Before(value = "intercept()", argNames = "joinPoint")
	public void intercept(JoinPoint joinPoint) {
		try {
			// Get the dirty map
			final Field dirtyField = DetachedEntityState.class
					.getDeclaredField("dirty");
			dirtyField.setAccessible(true);
			final DetachedEntityState<?> tgt = (DetachedEntityState<?>) joinPoint
					.getTarget();
			final Neo4jPersistentProperty uniqueProperty = tgt
					.getPersistentEntity().getUniqueProperty();
			final Map<Neo4jPersistentProperty, ?> dirty = (Map<Neo4jPersistentProperty, ?>) dirtyField
					.get(tgt);
			// Remove the uniqueProperty from the dirty map - it will be set as
			// part of the merge process already.
			dirty.remove(uniqueProperty);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
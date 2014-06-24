package me.dhallam.springdataneo4jdemo.utils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
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

	@Pointcut("(execution(* org.neo4j.kernel.impl.api.OperationsFacade.*(..)) || "
			+ "execution(* org.neo4j.kernel.impl.transaction.RWLock.*(..)) || "
			+ "execution(* org.springframework.data.neo4j.fieldaccess.DetachedEntityState.*(..)) || "
			+ "execution(* org.springframework.data.neo4j.support.query.CypherQueryEngineImpl.*(..)) || "
			+ "execution(* org.neo4j.kernel.impl.transaction.LockManagerImpl.*(..))"
			+ ") && !(execution(* *.toString(..)))")
	public void pointcuts() {
	}

	@Before(value = "pointcuts()", argNames = "joinPoint")
	public void pointcutsBefore(JoinPoint joinPoint) {
		debugAspect("Before", joinPoint);
	}

	@After(value = "pointcuts()", argNames = "joinPoint")
	public void pointcutsAfter(JoinPoint joinPoint) {
		debugAspect("After", joinPoint);
	}

	private void debugAspect(String prefix, JoinPoint joinPoint) {
		Object target = joinPoint.getTarget();
		LOG.info(
				"{} | {}({}) | target = {} | {}",
				prefix,
				joinPoint.getSignature().toShortString(),
				joinPoint.getArgs(),
				target.toString(),
				target.getClass().getName() + "@"
						+ Integer.toHexString(target.hashCode()));
	}

}
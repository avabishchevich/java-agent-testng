package com.zebrunner.agent.testng.core;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stores context of specific test method (not to be confused with specific test execution) belonging to specific test class instance
 */
@Getter
@Setter
public class TestMethodContext {

    /**
     * Represents current test method invocation. 0 means that method was not invoked yet (initial value for TestNG is 1).
     */
    private AtomicInteger currentInvocationCount = new AtomicInteger(0);

    /**
     * Index of list element - current index
     * Item value - old index
     */
    private Set<Integer> originalDataProviderIndices;

    /**
     * Marks that test is used by `dependsOnMethods` or `dependsOnGroups` from other test
     */
    private boolean forceRerun;

    /**
     * Represents size of data provider
     */
    private int dataProviderSize;

    /**
     * Represents current index of data provider
     */
    private int dataProviderCurrentIndex;

    public void incrementInvocationCount() {
        currentInvocationCount.incrementAndGet();
    }

    public int getCurrentInvocationCount() {
        return currentInvocationCount.get();
    }

}

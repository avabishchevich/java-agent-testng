package com.zebrunner.agent.testng.core.testname;

import com.zebrunner.agent.testng.listener.RunContextService;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultTestNameResolver implements TestNameResolver {

    private final ConcurrentHashMap<String, AtomicInteger> testNameToInvCounter = new ConcurrentHashMap<>();

    @Override
    public String resolve(ITestResult testResult) {
        String testName = getMethodName(testResult);

        testName = appendDataProviderLine(testResult, testName);
        testName = appendInvocationCount(testResult, testName);

        return testName;
    }

    private String appendDataProviderLine(ITestResult testResult, String testName) {
        ITestNGMethod testMethod = testResult.getMethod();
        ITestContext testContext = testResult.getTestContext();
        int dataProviderSize = RunContextService.getDataProviderSize(testMethod, testContext);

        if (dataProviderSize > 0) {
            // adding extra zero at the beginning of the data provider line number
            int indexMaxLength = Integer.toString(dataProviderSize).length() + 1;
            String lineFormat = " [L%0" + indexMaxLength + "d]";
            int index = RunContextService.getDataProviderCurrentIndex(testMethod, testContext) + 1;
            testName += String.format(lineFormat, index);
        }

        return testName;
    }

    private String appendInvocationCount(ITestResult testResult, String testName) {
        int expectedInvocationCount = getInvocationCount(testResult);
        if (expectedInvocationCount > 1) {
            // adding extra zero at the beginning of the invocation count (inspired by Vadim Delendik)
            int indexMaxLength = Integer.toString(expectedInvocationCount).length() + 1;
            String lineFormat = " [InvCount=%0" + indexMaxLength + "d]";

            int currentInvocationCount = testNameToInvCounter.computeIfAbsent(testName, $ -> new AtomicInteger(0))
                                                             .incrementAndGet();
            testName += String.format(lineFormat, currentInvocationCount);
        }
        return testName;
    }

    private int getInvocationCount(ITestResult testResult) {
        ITestNGMethod[] methods = testResult.getTestContext().getAllTestMethods();
        return Arrays.stream(methods)
                     .filter(method -> method.equals(testResult.getMethod()))
                     .findFirst()
                     .map(ITestNGMethod::getInvocationCount)
                     .orElse(0);
    }

    private String getMethodName(ITestResult testResult) {
        ITestNGMethod method = testResult.getMethod();
        Test testAnnotation = method.getConstructorOrMethod()
                                    .getMethod()
                                    .getAnnotation(Test.class);

        return Optional.ofNullable(testAnnotation)
                       .map(Test::testName)
                       .filter(testName -> !testName.trim().isEmpty())
                       .orElseGet(method::getMethodName);
    }

}

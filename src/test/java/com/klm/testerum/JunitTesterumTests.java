package com.klm.testerum;

import com.testerum.runner.junit5.TesterumJunitTestFactory;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.List;

public class JunitTesterumTests {

    @TestFactory
    public List<DynamicNode> testerumTestsFactory() {

        return new TesterumJunitTestFactory("KLM_tests")
                .variablesEnvironment("ute3")
                .getTests();
    }
}
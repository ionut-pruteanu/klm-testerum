package com.klm.testerum;

import com.testerum.runner.junit5.TesterumJunitTestFactory;
import java.util.List;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

public class JunitTesterumTests {

    @TestFactory
    public List<DynamicNode> testerumTestsFactory() {

        return new TesterumJunitTestFactory("KLM_tests")
                .getTests();
    }
}
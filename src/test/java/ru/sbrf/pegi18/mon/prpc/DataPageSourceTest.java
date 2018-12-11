package ru.sbrf.pegi18.mon.prpc;

import com.pega.pegarules.pub.runtime.ParameterPage;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author lapin2-aa
 */
class DataPageSourceTest {

    @Test
    void test() {
//        ParameterPage params = new ParameterPage();
//        params.putAll(new HashMap() {{
//            put("z", "x");
//        }});

        DataPageSource source = DataPageSource.builder()
            .ruleName("rn")
//            .parameterPage(params)
            .build();
        System.out.println(source.parameterPage());
    }

}
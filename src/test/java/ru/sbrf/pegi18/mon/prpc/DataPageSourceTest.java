package ru.sbrf.pegi18.mon.prpc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author lapin2-aa
 */
class DataPageSourceTest {

    @Test
    void test() {
        DataPageSource source = DataPageSource.builder().ruleName("rn").build();
        System.out.println(source.ruleName());
    }

}
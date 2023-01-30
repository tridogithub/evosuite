package com.examples.with.different.packagename;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.Add;

/**
 * @author tridv on 31/1/2023
 * @project evosuite
 */
public class UnitTest {
    @Test
    void test0() {
        Add add0 = new Add(2, 22, 277, 0);
    }

    @Test
    void test1() {
        Add add0 = new Add(2, 13, 327, 1);
        String string0 = add0.add(327, 0);
        Assertions.assertEquals("Nothing changed", string0);
    }


    @Test
    void test2() {
        Add add0 = new Add(2, 13, 327, 1);
        String string0 = add0.add(2, 2);
        Assertions.assertEquals("327/4/13 AD", string0);
    }


    @Test
    void test3() {
        Add add0 = new Add(2, 13, 327, 1);
        add0.setCurrentMonth(1209);
        String string0 = add0.add(0, 681);
        Assertions.assertEquals("Nothing changed", string0);
    }


    @Test
    void test4() {
        Add add0 = new Add(3, 13, 60, (-681));
        String string0 = add0.add(1, 1);
        Assertions.assertEquals("61/3/13 AD", string0);
    }


    @Test
    void test5() {
        Add add0 = new Add(3, 5, 335);
        String string0 = add0.add(3, 5);
        Assertions.assertEquals("2014/2/19 AD", string0);
    }


    @Test
    void test6() {
        Add add0 = new Add(6, 18, 62);
        String string0 = add0.add(1, (-834));
        Assertions.assertEquals("896/6/18 BC", string0);
    }


    @Test
    void test7() {
        Add add0 = new Add(8, 15, 365);
        add0.setCurrentYear((-613));
        String string0 = add0.add(1, (-613));
        Assertions.assertEquals("1/8/15 AD", string0);

        String string1 = add0.add(1, (-613));
        Assertions.assertEquals("613/8/15 BC", string1);
    }


    @Test
    void test8() {
        Add add0 = new Add(2, 17, 123, 94);
        String string0 = add0.add(2, (-2042));
        Assertions.assertEquals("48/1/17 BC", string0);
    }


    @Test
    void test9() {
        Add add0 = new Add(2, 13, 327, 1);
        add0.setCurrentERA(2);
        String string0 = add0.add(2, 2574);
        Assertions.assertEquals("113/8/13 AD", string0);
    }


    @Test
    void test10() {
        Add add0 = new Add(2, 17, 100, 1);
        String string0 = add0.add(2, 17);
        Assertions.assertEquals("101/7/17 AD", string0);
    }


    @Test
    void test11() {
        Add add0 = new Add(12, 31, 64);
        add0.setCurrentYear((-34));
        String string0 = add0.add(2, 5);
        Assertions.assertEquals("36/5/31 AD", string0);
    }


    @Test
    void test12() {
        Add add0 = new Add(2, 8, 2);
        String string0 = add0.add(2, (-1021));
        Assertions.assertEquals("87/2/8 BC", string0);
    }


    @Test
    void test13() {
        Add add0 = new Add(3, 13, 60);
        add0.setCurrentYear(3);
        String string0 = add0.add(1, 1);
        Assertions.assertEquals("2/3/13 BC", string0);
        String string1 = add0.add(1, 1);
        Assertions.assertEquals("1/3/13 BC", string1);
    }


    @Test
    void test14() {
        Add add0 = new Add(2, 8, 2);
        add0.setCurrentYear((-2156));
        String string0 = add0.add(2, (-1021));
        Assertions.assertEquals("2072/2/8 AD", string0);
    }


    @Test
    void test15() {
        Add add0 = new Add(2, 17, 123, 94);
        add0.setCurrentMonth(0);
        String string0 = add0.add(2369, 17);
        Assertions.assertEquals("Nothing changed", string0);
    }


    @Test
    void test16() {
        Add add0 = new Add(2, 17, 100, 1);
        add0.setCurrentDay((-1108));
        String string0 = add0.add(2, 248);
        Assertions.assertEquals("Nothing changed", string0);
    }


    @Test
    void test17() {
        Add add0 = new Add(11, 22, 228);
        add0.setCurrentDay(228);
        String string0 = add0.add((-2020), 22);
        Assertions.assertEquals("Nothing changed", string0);
    }


    @Test
    void test18() {
        Add add0 = new Add(2, 31, 210, (-1182));
        add0.setCurrentYear(3444);
        String string0 = add0.add(2, 2);
        Assertions.assertEquals("Nothing changed", string0);
    }


    @Test
    void test19() {
        Add add0 = new Add(2, 31, 210, (-1182));
        String string0 = add0.add(2, 2);
        Assertions.assertEquals("Nothing changed", string0);
    }


    @Test
    void test20() {
        Add add0 = new Add(3, 4, 3);
        add0.setCurrentMonth(4);
        String string0 = add0.add(4, 4);
        Assertions.assertEquals("Invalid field", string0);
    }


    @Test
    void test21() {
        Add add0 = new Add(9, 8, 189);
        String string0 = add0.add(9, 189);
        Assertions.assertEquals("Invalid field", string0);
    }


    @Test
    void test22() {
        Add add0 = new Add(11, 22, 228);
        String string0 = add0.add(22, 11);
        Assertions.assertEquals("Invalid field", string0);
    }

}

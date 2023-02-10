package com.examples.with.different.packagename;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ValidDateOrigin;

/**
 * @author tridv on 31/1/2023
 * @project evosuite
 */
public class UnitTest {
    @Test
    void test0() {
        ValidDateOrigin validDateOrigin0 = new ValidDateOrigin();
        boolean boolean0 = validDateOrigin0.isValidDateOld(2, 9, 2679);
        Assertions.assertTrue(boolean0);
    }


    @Test
    void test1() {
        ValidDateOrigin validDateOrigin0 = new ValidDateOrigin();
        boolean boolean0 = validDateOrigin0.isValidDateOld((-1276), (-179), 9);
        Assertions.assertFalse(boolean0);
    }


    @Test
    void test2() {
        ValidDateOrigin validDateOrigin0 = new ValidDateOrigin();
        boolean boolean0 = validDateOrigin0.isValidDateOld((-24), (-678), 2679);
        Assertions.assertFalse(boolean0);
    }


    @Test
    void test3() {
        ValidDateOrigin validDateOrigin0 = new ValidDateOrigin();
        boolean boolean0 = validDateOrigin0.isValidDateOld(324, 1560, 2523);
        Assertions.assertFalse(boolean0);
    }


    @Test
    void test4() {
        ValidDateOrigin validDateOrigin0 = new ValidDateOrigin();
        boolean boolean0 = validDateOrigin0.isValidDateOld(11, (-1), 3853);
        Assertions.assertFalse(boolean0);
    }


    @Test
    void test5() {
        ValidDateOrigin validDateOrigin0 = new ValidDateOrigin();
        boolean boolean0 = validDateOrigin0.isValidDateOld(11, 1071, 3390);
        Assertions.assertFalse(boolean0);
    }


    @Test
    void test6() {
        ValidDateOrigin validDateOrigin0 = new ValidDateOrigin();
        boolean boolean0 = validDateOrigin0.isValidDateOld(4, 4, 9999);
        Assertions.assertTrue(boolean0);
    }


    @Test
    void test7() {
        ValidDateOrigin validDateOrigin0 = new ValidDateOrigin();
        boolean boolean0 = validDateOrigin0.isValidDateOld(1, 6, 1923);
        Assertions.assertTrue(boolean0);
    }


    @Test
    void test8() {
        ValidDateOrigin validDateOrigin0 = new ValidDateOrigin();
        boolean boolean0 = validDateOrigin0.isValidDateOld(6, 1, 2496);
        Assertions.assertTrue(boolean0);
    }


    @Test
    void test9() {
        ValidDateOrigin validDateOrigin0 = new ValidDateOrigin();
        boolean boolean0 = validDateOrigin0.isValidDateOld(11, 28, 9999);
        Assertions.assertTrue(boolean0);
    }

}

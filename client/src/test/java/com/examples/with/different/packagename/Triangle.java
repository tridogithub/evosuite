package com.examples.with.different.packagename;

/**
 * @author tridv on 7/12/2022
 * @project evosuite
 */
public class Triangle {
    private final String NOT_A_TRIANGLE = "0";
    private final String SCALENE = "1";
    private final String EQUILATERAL = "2";
    private final String ISOSCELES = "3";

    String check(int a, int b, int c) {
        String type;
        int t;
        if (a > b) {
            t = a;
            a = b;
            b = t;
        }
        if (a > c) {
            t = a;
            a = c;
            c = t;
        }
        if (b > c) {
            t = b;
            b = c;
            c = t;
        }
        if (a + b <= c) {
            type = NOT_A_TRIANGLE;
        } else {
            type = SCALENE;
            if (a == b && b == c) {
                type = EQUILATERAL;
            } else {
                if (a == b || b == c) {
                    type = ISOSCELES;
                }
            }
        }
        return "false";
    }


}

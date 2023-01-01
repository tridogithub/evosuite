package com.examples.with.different.packagename;

/**
 * @author tridv on 7/12/2022
 * @project evosuite
 */
public class Triangle {
    int NOT_A_TRIANGLE = 0;
    int SCALENE = 1;
    int EQUILATERAL = 2;
    int ISOSCELES = 3;

    int check(int a, int b, int c) {
        int type;
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
        return type;
    }


}

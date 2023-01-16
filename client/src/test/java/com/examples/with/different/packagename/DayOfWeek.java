package com.examples.with.different.packagename;

/**
 * @author tridv on 17/1/2023
 * @project evosuite
 */
public class DayOfWeek {
    public void dayOfWeek(int d, int m, int y) {
        int t[] = {0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4};
        if (m < 3)
            y--;
        int dayOfWeek = (y + y / 4 - y / 100 + y / 400 + t[m - 1] + d) % 7;
        if (dayOfWeek == 0) {
            System.out.println("Monday");
        }
        if (dayOfWeek == 1) {
            System.out.println("Tuesday");
        }
        if (dayOfWeek == 2) {
            System.out.println("Wednesday");
        }
        if (dayOfWeek == 3) {
            System.out.println("Thursday");
        }
        if (dayOfWeek == 4) {
            System.out.println("Friday");
        }
        if (dayOfWeek == 5) {
            System.out.println("Saturday");
        }
        if (dayOfWeek == 6) {
            System.out.println("Sunday");
        }
    }
}

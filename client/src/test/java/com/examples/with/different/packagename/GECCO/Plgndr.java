package com.examples.with.different.packagename.GECCO;

/**
 * @author tridv on 1/7/2023
 * @project evosuite
 */
public class Plgndr {

    public double plgndr(int l, int m, double x) {
        double fact, pll = 0, pmm, pmmp1, somx2;

        int i, ll;
        if (m < 0 || m > l || Math.abs(x) > 1.0)
            System.out.println("Bad arguments in routine plgndr");
        pmm = 1.0;
        if (m > 0) {
            somx2 = Math.sqrt((1.0 - x) * (1.0 + x));
            fact = 1.0;
            for (i = 1; i <= m; i++) {
                pmm *= -fact * somx2;
                fact += 2.0;
            }
        }
        if (l == m)
            return pmm;
        else {
            pmmp1 = x * (2 * m + 1) * pmm;
            if (l == (m + 1))
                return pmmp1;
            else {
                for (ll = m + 2; ll <= l; ll++) {
                    pll = (x * (2 * ll - 1) * pmmp1 - (ll + m - 1) * pmm) / (ll - m);
                    pmm = pmmp1;
                    pmmp1 = pll;
                }
                return pll;
            }
        }
    }
}

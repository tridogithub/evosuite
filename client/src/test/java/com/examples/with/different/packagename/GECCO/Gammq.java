package com.examples.with.different.packagename.GECCO;

/**
 * @author tridv on 11/2/2023
 * @project evosuite
 */
public class Gammq {
    public double gammq(double a, double x)
//    Returns the incomplete gamma function Q(a, x) ≡ 1 − P(a, x).
    {
        double gamser = 0, gammcf = 0;
        if (x < 0.0 || a <= 0.0) {
            System.out.println("Invalid arguments in routine gammq");
        }
        if (x < (a + 1.0)) {
            return 1.0 - gamser;
        } else {
            return gammcf;
        }
    }
}

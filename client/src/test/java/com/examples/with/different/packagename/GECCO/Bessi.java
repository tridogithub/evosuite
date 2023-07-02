package com.examples.with.different.packagename.GECCO;

/**
 * @author tridv on 1/7/2023
 * @project evosuite
 */
public class Bessi {
    private final double ACC = 40.0;
    private final double BIGNO = 1.0e10;
    private final double BIGNI = 1.0e-10;

    public double bessi(int n, double x) {
        int j;
        double bi, bim, bip, tox, ans;
        if (n < 2) System.out.println("Index n less than 2 in bessi");
        if (x == 0.0)
            return 0.0;
        else {
            tox = 2.0 / Math.abs(x);
            bip = ans = 0.0;
            bi = 1.0;
            for (j = 2 * (n + (int) Math.sqrt(ACC * n)); j > 0; j--) {
//                Downward recurrence from even m.
                bim = bip + j * tox * bi;
                bip = bi;
                bi = bim;
                if (Math.abs(bi) > BIGNO) {
//                    Renormalize to prevent overflows.
                    ans *= BIGNI;
                    bi *= BIGNI;
                    bip *= BIGNI;
                }
                if (j == n) ans = bip;
            }
            ans *= bessi0(x) / bi;
//            Normalize with bessi0.
            return x < 0.0 && (n & 1) != 0 ? -ans : ans;
        }
    }

    private double bessi0(double x) {
        double ax, ans;
        double y;
//        Accumulate polynomials in double precision.
        if ((ax = Math.abs(x)) < 3.75) {
//            Polynomial fit.
            y = x / 3.75;
            y *= y;
            ans = 1.0 + y * (3.5156229 + y * (3.0899424 + y * (1.2067492
                    + y * (0.2659732 + y * (0.360768e-1 + y * 0.45813e-2)))));
        } else {
            y = 3.75 / ax;
            ans = (Math.exp(ax) / Math.sqrt(ax)) * (0.39894228 + y * (0.1328592e-1
                    + y * (0.225319e-2 + y * (-0.157565e-2 + y * (0.916281e-2
                    + y * (-0.2057706e-1 + y * (0.2635537e-1 + y * (-0.1647633e-1
                    + y * 0.392377e-2))))))));
        }
        return ans;
    }
}

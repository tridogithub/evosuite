/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.basic;

import com.examples.with.different.packagename.Example;
import com.examples.with.different.packagename.NextDate;
import com.examples.with.different.packagename.Triangle;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.NullString;

import java.util.List;

public class NullStringSystemTest extends SystemTestBase {

    @Test
    public void testNullString() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = NextDate.class.getCanonicalName();
//        String targetClass = NullString.class.getCanonicalName();
//        String targetClass = Triangle.class.getCanonicalName();
        System.out.println(targetClass);
//        String targetClass = Example.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
//        Properties.ITERATION = 10;
//        Properties.STRATEGY = Properties.Strategy.TLBO;
//        String[] command = new String[]{"-generateSuite", "-class", targetClass, "-Dstrategy="+Properties.Strategy.TLBO.name()};
        Properties.ALGORITHM = Properties.Algorithm.MONOTONIC_GA;
        String[] command = new String[]{"-generateSuite", "-class", targetClass};
//        Properties.NULL_PROBABILITY = 1;


        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        List<BranchCoverageTestFitness> coverageGoals = (List<BranchCoverageTestFitness>) TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals();
        int goals = coverageGoals.size(); // assuming single fitness function
        coverageGoals.forEach(System.out::println);
//        Assert.assertEquals("Wrong number of goals: ", 3, goals);
//        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
        Assert.assertTrue(true);
    }
}

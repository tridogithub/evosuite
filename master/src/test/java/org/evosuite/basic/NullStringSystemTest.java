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

import com.examples.with.different.packagename.GECCO.Bessj;
import com.examples.with.different.packagename.GECCO.Expint;
import com.examples.with.different.packagename.GECCO.Gammq;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NullStringSystemTest extends SystemTestBase {

    @Test
    public void testNullString() {
        List<Double> coverageList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            EvoSuite evosuite = new EvoSuite();

//        String targetClass = Triangle.class.getCanonicalName();
//            String targetClass = NextDate.class.getCanonicalName();
//        String targetClass = NextDateOrigin.class.getCanonicalName();
//        String targetClass = DayOfWeek.class.getCanonicalName();
//        String targetClass = Add.class.getCanonicalName();
//        String targetClass = AddOrigin.class.getCanonicalName();
//        String targetClass = ValidDate.clas   s.getCanonicalName();
//        String targetClass = ValidDateOrqigin.class.getCanonicalName();
//        String targetClass = Example.class.getCanonicalName();
//        String targetClass = TreeSet.class.getCanonicalName();
//        String targetClass = NextDateOriginal.class.getCanonicalName();
//        String targetClass = NullString.class.getCanonicalName();
//        String targetClass = Triangle.class.getCanonicalName();

//        String targetClass = Bessj.class.getCanonicalName();
        String targetClass = Expint.class.getCanonicalName();
//        String targetClass = Gammq.class.getCanonicalName();
//        String targetClass = EI.class.getCanonicalName();
//        System.out.println(targetClass);

            Properties.REDUCE_SPACE = true;
            Properties.PROPOSED_DC = true;
//            Properties.BESSJ_DC = true;
//            Properties.GAMMQ_DC = true;
            Properties.EXPINT_DC = true;
//            Properties.NEXT_DATE_DC = true;
//            Properties.VALID_DATE_DC = true;
//            Properties.ADD_DATE_DC = true;
//            Properties.DIFFICULTY_EFFICIENT_ARRAY = Properties.NEXT_DATE_DIFFICULTY_COEFFICIENT_MAP;
//        Properties.DIFFICULTY_EFFICIENT_ARRAY = Properties.VALID_DATE_DIFFICULTY_COEFFICIENT_MAP;
//        Properties.DIFFICULTY_EFFICIENT_ARRAY = Properties.ADD_DATE_DIFFICULTY_COEFFICIENT_MAP;

//        Properties.SAKTI_DC = true;
//        Properties.SAKTI_DIFFICULTY_EFFICIENT_ARRAY = Properties.SAKTI_NEXT_DATE_DIFFICULTY_COEFFICIENT_MAP;
//        Properties.SAKTI_DIFFICULTY_EFFICIENT_ARRAY = Properties.SAKTI_VALID_DATE_DIFFICULTY_COEFFICIENT_MAP;
//        Properties.SAKTI_DIFFICULTY_EFFICIENT_ARRAY = Properties.SAKTI_ADD_DATE_DIFFICULTY_COEFFICIENT_MAP;

            Properties.TARGET_CLASS = targetClass;
            Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.BRANCH};
//        Properties.ITERATION = 10;
//        Properties.STRATEGY = Properties.Strategy.TLBO;
//        String[] command = new String[]{"-generateSuite", "-class", targetClass, "-Dstrategy="+Properties.Strategy.TLBO.name()};
            Properties.RANDOM_SEED = System.currentTimeMillis();
            Properties.ALGORITHM = Properties.Algorithm.MONOTONIC_GA;
            String[] command = new String[]{"-generateSuite", "-class", targetClass};
//        String[] command = new String[]{"-generateTests", "-class", targetClass};
//        Properties.NULL_PROBABILITY = 1;


            Object result = evosuite.parseCommandLine(command);
            GeneticAlgorithm<?> ga = getGAFromResult(result);
            TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
            coverageList.add(best.getCoverage());
//        int numberOfNotCoveredGoals = best.getNumOfNotCoveredGoals();
//        LinkedHashMap<FitnessFunction<TestSuiteChromosome>, Integer> uncoveredGoals = best.getNumsNotCoveredGoals();
//        System.out.println("EvolvedTestSuite:\n" + best);

            List<BranchCoverageTestFitness> coverageGoals = (List<BranchCoverageTestFitness>) TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals();
            int goals = coverageGoals.size(); // assuming single fitness function
//        coverageGoals.forEach(System.out::println);
//        Assert.assertEquals("Wrong number of goals: ", 3, goals);
//        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
            setDefaultPropertiesForTestCases();
        }
        System.out.println(coverageList.stream().map(value -> String.valueOf(value)).collect(Collectors.joining(", ")));
        System.out.println(String.format("Average coverage with %s times tried: %s",
                coverageList.size(), coverageList.stream().mapToDouble(value -> value.doubleValue()).sum() / coverageList.size()));
    }

}

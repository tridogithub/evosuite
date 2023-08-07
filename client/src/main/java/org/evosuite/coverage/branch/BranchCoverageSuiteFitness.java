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
package org.evosuite.coverage.branch;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.ga.archive.Archive;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.ActualControlFlowGraph;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.CFGMethodAdapter;
import org.evosuite.graphs.cfg.ControlDependency;
import org.evosuite.graphs.cfg.ControlFlowEdge;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Fitness function for a whole test suite for all branches
 *
 * @author Gordon Fraser
 */
public class BranchCoverageSuiteFitness extends TestSuiteFitnessFunction {

    private static final long serialVersionUID = 2991632394620406243L;

    private final static Logger logger = LoggerFactory.getLogger(BranchCoverageSuiteFitness.class);

    // Coverage targets
    public int totalGoals;
    public int totalMethods;
    public int totalBranches;
    private final Set<String> branchlessMethods = new LinkedHashSet<>();
    private final Set<String> methods = new LinkedHashSet<>();

    protected final Set<Integer> branchesId = new LinkedHashSet<>();

    // Some stuff for debug output
    public int maxCoveredBranches = 0;
    public int maxCoveredMethods = 0;
    public double bestFitness = Double.MAX_VALUE;

    // Each test gets a set of distinct covered goals, these are mapped by branch id
    protected transient Map<Integer, TestFitnessFunction> branchCoverageTrueMap = new LinkedHashMap<>();
    protected transient Map<Integer, TestFitnessFunction> branchCoverageFalseMap = new LinkedHashMap<>();
    private transient Map<String, TestFitnessFunction> branchlessMethodCoverageMap = new LinkedHashMap<>();

    private final Set<Integer> toRemoveBranchesT = new LinkedHashSet<>();
    private final Set<Integer> toRemoveBranchesF = new LinkedHashSet<>();
    private final Set<String> toRemoveRootBranches = new LinkedHashSet<>();

    private final Set<Integer> removedBranchesT = new LinkedHashSet<>();
    private final Set<Integer> removedBranchesF = new LinkedHashSet<>();
    private final Set<String> removedRootBranches = new LinkedHashSet<>();
    private Map<Integer, Double> branchDifficultyCoefficient = new HashMap<>();
    private transient BranchPool branchPool;
    private transient GraphPool graphPool;

    /**
     * <p>
     * Constructor for BranchCoverageSuiteFitness.
     * </p>
     */
    public BranchCoverageSuiteFitness() {

        this(TestGenerationContext.getInstance().getClassLoaderForSUT());
    }

    /**
     * <p>
     * Constructor for BranchCoverageSuiteFitness.
     * </p>
     */
    public BranchCoverageSuiteFitness(ClassLoader classLoader) {
        String prefix = Properties.TARGET_CLASS_PREFIX;

        if (prefix.isEmpty())
            prefix = Properties.TARGET_CLASS;

        totalMethods = CFGMethodAdapter.getNumMethodsPrefix(classLoader, prefix);
        totalBranches = BranchPool.getInstance(classLoader).getBranchCountForPrefix(prefix);
        branchlessMethods.addAll(BranchPool.getInstance(classLoader).getBranchlessMethodsPrefix(prefix));
        methods.addAll(CFGMethodAdapter.getMethodsPrefix(classLoader, prefix));

        determineCoverageGoals(true);

        totalGoals = branchCoverageTrueMap.size() + branchCoverageFalseMap.size() + branchlessMethodCoverageMap.size();

        logger.info("Total branch coverage goals: " + totalGoals);
        logger.info("Total branches: " + totalBranches);
        logger.info("Total branchless methods: " + branchlessMethodCoverageMap.size());
        logger.info("Total methods: " + totalMethods + ": " + methods);

        branchPool = BranchPool.getInstance(classLoader);
        graphPool = GraphPool.getInstance(classLoader);
    }

    /**
     * Initialize the set of known coverage goals
     */
    protected void determineCoverageGoals(boolean updateArchive) {
        List<BranchCoverageTestFitness> goals = new BranchCoverageFactory().getCoverageGoals();
        for (BranchCoverageTestFitness goal : goals) {
            // Skip instrumented branches - we only want real branches
            if (goal.getBranch() != null) {
                if (goal.getBranch().isInstrumented()) {
                    continue;
                }
            }
            if (updateArchive && Properties.TEST_ARCHIVE)
                Archive.getArchiveInstance().addTarget(goal);

            if (goal.getBranch() == null) {
                branchlessMethodCoverageMap.put(goal.getClassName() + "."
                        + goal.getMethod(), goal);
            } else {
                branchesId.add(goal.getBranch().getActualBranchId());
                if (goal.getBranchExpressionValue())
                    branchCoverageTrueMap.put(goal.getBranch().getActualBranchId(), goal);
                else
                    branchCoverageFalseMap.put(goal.getBranch().getActualBranchId(), goal);
            }
        }
    }

    /**
     * If there is an exception in a superconstructor, then the corresponding
     * constructor might not be included in the execution trace
     *
     * @param result
     * @param callCount
     */
    private void handleConstructorExceptions(TestChromosome test, ExecutionResult result,
                                             Map<String, Integer> callCount) {

        if (result.hasTimeout() || result.hasTestException() || result.noThrownExceptions()) {
            return;
        }

        Integer exceptionPosition = result.getFirstPositionOfThrownException();
        // TODO: Not sure why that can happen
        if (exceptionPosition >= result.test.size()) {
            return;
        }

        Statement statement = null;
        if (result.test.hasStatement(exceptionPosition)) {
            statement = result.test.getStatement(exceptionPosition);
        }
        if (statement instanceof ConstructorStatement) {
            ConstructorStatement c = (ConstructorStatement) statement;
            String className = c.getConstructor().getName();
            String methodName = "<init>" + Type.getConstructorDescriptor(c.getConstructor().getConstructor());
            String name = className + "." + methodName;
            if (!callCount.containsKey(name)) {
                callCount.put(name, 1);
                if (branchlessMethodCoverageMap.containsKey(name)) {
                    TestFitnessFunction goal = branchlessMethodCoverageMap.get(name);
                    test.getTestCase().addCoveredGoal(goal);
                    toRemoveRootBranches.add(name);
                    if (Properties.TEST_ARCHIVE) {
                        Archive.getArchiveInstance().updateArchive(goal, test, 0.0);
                    }
                }

            }
        }
    }

    protected void handleBranchlessMethods(TestChromosome test, ExecutionResult result, Map<String, Integer> callCount) {
        for (Entry<String, Integer> entry : result.getTrace().getMethodExecutionCount().entrySet()) {

            if (entry.getKey() == null || !methods.contains(entry.getKey()) || removedRootBranches.contains(entry.getKey()))
                continue;
            if (!callCount.containsKey(entry.getKey()))
                callCount.put(entry.getKey(), entry.getValue());
            else {
                callCount.put(entry.getKey(),
                        callCount.get(entry.getKey()) + entry.getValue());
            }
            // If a specific target method is set we need to check
            // if this is a target branch or not
            if (branchlessMethodCoverageMap.containsKey(entry.getKey())) {
                TestFitnessFunction goal = branchlessMethodCoverageMap.get(entry.getKey());
                test.getTestCase().addCoveredGoal(goal);
                toRemoveRootBranches.add(entry.getKey());
                if (Properties.TEST_ARCHIVE) {
                    Archive.getArchiveInstance().updateArchive(goal, test, 0.0);
                }
            }
        }
    }

    protected void handlePredicateCount(ExecutionResult result, Map<Integer, Integer> predicateCount) {
        for (Entry<Integer, Integer> entry : result.getTrace().getPredicateExecutionCount().entrySet()) {
            if (!branchesId.contains(entry.getKey())
                    || (removedBranchesT.contains(entry.getKey())
                    && removedBranchesF.contains(entry.getKey())))
                continue;
            if (!predicateCount.containsKey(entry.getKey()))
                predicateCount.put(entry.getKey(), entry.getValue());
            else {
                predicateCount.put(entry.getKey(),
                        predicateCount.get(entry.getKey())
                                + entry.getValue());
            }
        }
    }


    protected void handleTrueDistances(TestChromosome test, ExecutionResult result, Map<Integer, Double> trueDistance) {
        for (Entry<Integer, Double> entry : result.getTrace().getTrueDistances().entrySet()) {
            if (!branchesId.contains(entry.getKey()) || removedBranchesT.contains(entry.getKey())) continue;
            BranchCoverageTestFitness goal = (BranchCoverageTestFitness) this.branchCoverageTrueMap.get(entry.getKey());
            assert goal != null;
            //// Author's code ////
            if (!trueDistance.containsKey(entry.getKey()))
                trueDistance.put(entry.getKey(), entry.getValue());
            else {
                trueDistance.put(entry.getKey(),
                        Math.min(trueDistance.get(entry.getKey()),
                                entry.getValue()));
            }
            //// Author's code ////

            //// New Code ////
//            int lineNumber = goal.getBranchGoal().getLineNumber();
//            if (!trueDistance.containsKey(entry.getKey())) {
//                if (branchDifficultyCoefficient.containsKey(lineNumber)) {
//                    trueDistance.put(entry.getKey(), entry.getValue() * branchDifficultyCoefficient.get(lineNumber));
//                } else {
//                    trueDistance.put(entry.getKey(), entry.getValue());
//                }
//            } else {
//                if (branchDifficultyCoefficient.containsKey(lineNumber)) {
//                    trueDistance.put(entry.getKey(), Math.min(trueDistance.get(entry.getKey()), entry.getValue())
//                            * branchDifficultyCoefficient.get(lineNumber)
//                    );
//                } else {
//                    trueDistance.put(entry.getKey(),
//                            Math.min(trueDistance.get(entry.getKey()),
//                                    entry.getValue()));
//                }
//            }
            //// New Code ////

            if ((Double.compare(entry.getValue(), 0.0) == 0)) {
                test.getTestCase().addCoveredGoal(goal);
                toRemoveBranchesT.add(entry.getKey());
            }
            if (Properties.TEST_ARCHIVE) {
                Archive.getArchiveInstance().updateArchive(goal, test, entry.getValue());
            }
        }

    }

    protected void handleFalseDistances(TestChromosome test, ExecutionResult result, Map<Integer, Double> falseDistance) {
        for (Entry<Integer, Double> entry : result.getTrace().getFalseDistances().entrySet()) {
            if (!branchesId.contains(entry.getKey()) || !branchCoverageFalseMap.containsKey(entry.getKey()) || removedBranchesF.contains(entry.getKey()))
                continue;
            BranchCoverageTestFitness goal = (BranchCoverageTestFitness) this.branchCoverageFalseMap.get(entry.getKey());
            assert goal != null;

            //// Author's code ////
            if (!falseDistance.containsKey(entry.getKey()))
                falseDistance.put(entry.getKey(), entry.getValue());
            else {
                falseDistance.put(entry.getKey(),
                        Math.min(falseDistance.get(entry.getKey()),
                                entry.getValue()));
            }
            //// Author's code ////

            //// New code ////
//            int lineNumber = goal.getBranchGoal().getLineNumber();
//            if (!falseDistance.containsKey(entry.getKey())) {
//                if (branchDifficultyCoefficient.containsKey(lineNumber)) {
//                    falseDistance.put(entry.getKey(), entry.getValue() * branchDifficultyCoefficient.get(lineNumber));
//                } else {
//                    falseDistance.put(entry.getKey(), entry.getValue());
//                }
//            } else {
//                if (branchDifficultyCoefficient.containsKey(lineNumber)) {
//                    falseDistance.put(entry.getKey(), Math.min(falseDistance.get(entry.getKey()), entry.getValue())
//                            * branchDifficultyCoefficient.get(lineNumber)
//                    );
//                } else {
//                    falseDistance.put(entry.getKey(), Math.min(falseDistance.get(entry.getKey()), entry.getValue()));
//                }
//            }
            //// New code ////

            if ((Double.compare(entry.getValue(), 0.0) == 0)) {
                test.getTestCase().addCoveredGoal(goal);
                toRemoveBranchesF.add(entry.getKey());
            }
            if (Properties.TEST_ARCHIVE) {
                Archive.getArchiveInstance().updateArchive(goal, test, entry.getValue());
            }
        }

    }

    /**
     * Iterate over all execution results and summarize statistics
     *
     * @param results
     * @param predicateCount
     * @param callCount
     * @param trueDistance
     * @param falseDistance
     * @return
     */
    private boolean analyzeTraces(TestSuiteChromosome suite,
                                  List<ExecutionResult> results,
                                  Map<Integer, Integer> predicateCount, Map<String, Integer> callCount,
                                  Map<Integer, Double> trueDistance, Map<Integer, Double> falseDistance) {
        boolean hasTimeoutOrTestException = false;
        for (ExecutionResult result : results) {
            if (result.hasTimeout() || result.hasTestException()) {
                hasTimeoutOrTestException = true;
                continue;
            }

            TestChromosome test = new TestChromosome();
            test.setTestCase(result.test);
            test.setLastExecutionResult(result);
            test.setChanged(false);

            handleBranchlessMethods(test, result, callCount);
            handlePredicateCount(result, predicateCount);
            handleTrueDistances(test, result, trueDistance);
            handleFalseDistances(test, result, falseDistance);

            // In case there were exceptions in a constructor
            handleConstructorExceptions(test, result, callCount);
        }
        return hasTimeoutOrTestException;
    }

    @Override
    public boolean updateCoveredGoals() {
        if (!Properties.TEST_ARCHIVE) {
            return false;
        }

        for (String method : toRemoveRootBranches) {
            boolean removed = branchlessMethods.remove(method);
            TestFitnessFunction f = branchlessMethodCoverageMap.remove(method);
            if (removed && f != null) {
                totalMethods--;
                methods.remove(method);
                removedRootBranches.add(method);
                //removeTestCall(f.getTargetClass(), f.getTargetMethod());
            } else {
                throw new IllegalStateException("goal to remove not found");
            }
        }

        for (Integer branch : toRemoveBranchesT) {
            TestFitnessFunction f = branchCoverageTrueMap.remove(branch);
            if (f != null) {
                removedBranchesT.add(branch);
                if (removedBranchesF.contains(branch)) {
                    totalBranches--;
                    //if(isFullyCovered(f.getTargetClass(), f.getTargetMethod())) {
                    //	removeTestCall(f.getTargetClass(), f.getTargetMethod());
                    //}
                }
            } else {
                throw new IllegalStateException("goal to remove not found");
            }
        }
        for (Integer branch : toRemoveBranchesF) {
            TestFitnessFunction f = branchCoverageFalseMap.remove(branch);
            if (f != null) {
                removedBranchesF.add(branch);
                if (removedBranchesT.contains(branch)) {
                    totalBranches--;
                    //if(isFullyCovered(f.getTargetClass(), f.getTargetMethod())) {
                    //	removeTestCall(f.getTargetClass(), f.getTargetMethod());
                    //}
                }
            } else {
                throw new IllegalStateException("goal to remove not found");
            }
        }

        toRemoveRootBranches.clear();
        toRemoveBranchesF.clear();
        toRemoveBranchesT.clear();
        logger.info("Current state of archive: " + Archive.getArchiveInstance().toString());

        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Execute all tests and count covered branches
     */
    @Override
    public double getFitness(TestSuiteChromosome suite) {
        Map<String, ActualControlFlowGraph> actualControlFlowGraphMap = graphPool.getActualCFGs(Properties.TARGET_CLASS);
        Map<Integer, List<ControlDependency>> nodeAndPathMap = getBranchDependencies(actualControlFlowGraphMap);

        logger.trace("Calculating branch fitness");
        double fitness = 0.0;

        List<ExecutionResult> results = runTestSuite(suite);
        Map<Integer, Integer> trueBranchIdAndLineNumberMap
                = this.branchCoverageTrueMap.entrySet().stream().collect(Collectors.toMap(
                Entry::getKey,
                e -> ((BranchCoverageTestFitness) e.getValue()).getBranchGoal().getLineNumber()
        ));
        Map<Integer, Integer> falseBranchIdAndLineNumberMap
                = this.branchCoverageFalseMap.entrySet().stream().collect(Collectors.toMap(
                Entry::getKey,
                e -> ((BranchCoverageTestFitness) e.getValue()).getBranchGoal().getLineNumber()
        ));

        Map<Integer, Double> trueDistance = new LinkedHashMap<>();
        Map<Integer, Double> falseDistance = new LinkedHashMap<>();
        Map<Integer, Integer> predicateCount = new LinkedHashMap<>();
        Map<String, Integer> callCount = new LinkedHashMap<>();

        // Set up branch DC
        setupBranchDC();

        // Collect stats in the traces
        boolean hasTimeoutOrTestException = analyzeTraces(suite, results, predicateCount,
                callCount, trueDistance,
                falseDistance);

        // Collect branch distances of covered branches
        int numCoveredBranches = 0;

        for (Integer key : predicateCount.keySet()) {

            double df = 0.0;
            double dt = 0.0;
            int numExecuted = predicateCount.get(key);

            if (removedBranchesT.contains(key))
                numExecuted++;
            if (removedBranchesF.contains(key))
                numExecuted++;

            if (trueDistance.containsKey(key)) {
//                dt = trueDistance.get(key);
//                if (!this.toRemoveBranchesT.contains(key) && Properties.PROPOSED_DC) {
//                    // Branch is not covered
//                    Integer lineNumber = trueBranchIdAndLineNumberMap.get(key);
//                    double dc = Optional.ofNullable(branchDifficultyCoefficient.get(lineNumber)).orElse(1d);
//                    dt = trueDistance.get(key) * dc;
//                } else {
                dt = trueDistance.get(key);
//                }
            }
            if (falseDistance.containsKey(key)) {
//                df = falseDistance.get(key);
//                if (!this.toRemoveBranchesF.contains(key) && Properties.PROPOSED_DC) {
//                    Integer lineNumber = falseBranchIdAndLineNumberMap.get(key);
//                    double dc = Optional.ofNullable(branchDifficultyCoefficient.get(lineNumber)).orElse(1d);
//                    df = falseDistance.get(key) * dc;
//                } else {
                df = falseDistance.get(key);
//                }
            }
            // If the branch predicate was only executed once, then add 1
            if (numExecuted == 1) {
                fitness += 1.0;
            } else {
                fitness += normalize(df) + normalize(dt);
            }

            if (falseDistance.containsKey(key) && (Double.compare(df, 0.0) == 0))
                numCoveredBranches++;

            if (trueDistance.containsKey(key) && (Double.compare(dt, 0.0) == 0))
                numCoveredBranches++;
        }

        if (Properties.PROPOSED_DC || Properties.SAKTI_DC) {
            // Add DC value
//            for (Map.Entry<Integer, TestFitnessFunction> entry : branchCoverageTrueMap.entrySet()) {
//                if (!predicateCount.containsKey(entry.getKey())) {
//                    BranchCoverageTestFitness branchCoverageTestFitness = (BranchCoverageTestFitness) entry.getValue();
//                    int lineNumber = branchCoverageTestFitness.getBranchGoal().getLineNumber();
//                    Double dcValue = branchDifficultyCoefficient.get(lineNumber);
//                    fitness += dcValue != null ? dcValue.doubleValue() : 0.0;
//                }
//            }

            // Get node to active DC value (all node belongs to the path lead to uncovered node)
            Set<Integer> setOfDCNode = new HashSet<>();
            for (Map.Entry<Integer, TestFitnessFunction> entry : branchCoverageTrueMap.entrySet()) {
                if (!predicateCount.containsKey(entry.getKey()) && nodeAndPathMap.containsKey(entry.getKey())) {
                    Set<Integer> allPathNodes = getAllPathNodes(entry.getKey(), nodeAndPathMap);
                    setOfDCNode.addAll(allPathNodes);
                }
            }
            String activeDCNodes = setOfDCNode.stream().map(i -> String.valueOf(i)).collect(Collectors.joining(", "));
//            appendToFile("ActiveDCNode.txt", activeDCNodes);
            for (Integer integer : setOfDCNode) {
                Branch branch = branchPool.getBranch(integer);
                if (branch != null) {
                    int lineNumber = branch.getInstruction().getLineNumber();
                    Double dcValue = branchDifficultyCoefficient.get(lineNumber);
                    fitness += dcValue != null ? dcValue.doubleValue() : 0.0;
                }
            }
        }

        // +1 for every branch that was not executed
        fitness += 2 * (totalBranches - predicateCount.size());

        // Ensure all methods are called
        int missingMethods = 0;
        for (String e : methods) {
            if (!callCount.containsKey(e)) {
                fitness += 1.0;
                missingMethods += 1;
            }
        }
        printStatusMessages(suite, numCoveredBranches, totalMethods - missingMethods,
                fitness);

        // Calculate coverage
        int coverage = numCoveredBranches;
        for (String e : branchlessMethodCoverageMap.keySet()) {
            if (callCount.containsKey(e)) {
                coverage++;
            }

        }

        coverage += removedBranchesF.size();
        coverage += removedBranchesT.size();
        coverage += removedRootBranches.size();


        if (totalGoals > 0)
            suite.setCoverage(this, (double) coverage / (double) totalGoals);
        else
            suite.setCoverage(this, 1);

        suite.setNumOfCoveredGoals(this, coverage);
        suite.setNumOfNotCoveredGoals(this, totalGoals - coverage);

        if (hasTimeoutOrTestException) {
            logger.info("Test suite has timed out, setting fitness to max value "
                    + (totalBranches * 2 + totalMethods));
            fitness = totalBranches * 2 + totalMethods;
            //suite.setCoverage(0.0);
        }

        updateIndividual(suite, fitness);

        assert (coverage <= totalGoals) : "Covered " + coverage + " vs total goals "
                + totalGoals;
        assert (fitness >= 0.0);
        assert (fitness != 0.0 || coverage == totalGoals) : "Fitness: " + fitness + ", "
                + "coverage: " + coverage + "/" + totalGoals;
        assert (suite.getCoverage(this) <= 1.0) && (suite.getCoverage(this) >= 0.0) : "Wrong coverage value "
                + suite.getCoverage(this);
        return fitness;
    }


    /*
     * Max branch coverage value
     */
    public int getMaxValue() {
        return totalBranches * 2 + totalMethods;
    }

    /**
     * Some useful debug information
     *
     * @param coveredBranches
     * @param coveredMethods
     * @param fitness
     */
    private void printStatusMessages(TestSuiteChromosome suite,
                                     int coveredBranches, int coveredMethods, double fitness) {
        if (coveredBranches > maxCoveredBranches) {
            maxCoveredBranches = coveredBranches;
            logger.info("(Branches) Best individual covers " + coveredBranches + "/"
                    + (totalBranches * 2) + " branches and " + coveredMethods + "/"
                    + totalMethods + " methods");
            logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
                    + suite.totalLengthOfTestCases());
        }
        if (coveredMethods > maxCoveredMethods) {
            logger.info("(Methods) Best individual covers " + coveredBranches + "/"
                    + (totalBranches * 2) + " branches and " + coveredMethods + "/"
                    + totalMethods + " methods");
            maxCoveredMethods = coveredMethods;
            logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
                    + suite.totalLengthOfTestCases());
        }
        if (fitness < bestFitness) {
            logger.info("(Fitness) Best individual covers " + coveredBranches + "/"
                    + (totalBranches * 2) + " branches and " + coveredMethods + "/"
                    + totalMethods + " methods");
            bestFitness = fitness;
            logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
                    + suite.totalLengthOfTestCases());
        }
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();

        branchCoverageTrueMap = new LinkedHashMap<>();
        branchCoverageFalseMap = new LinkedHashMap<>();
        branchlessMethodCoverageMap = new LinkedHashMap<>();

        determineCoverageGoals(false);
    }

    private Map<Integer, List<ControlDependency>> getBranchDependencies(Map<String, ActualControlFlowGraph> actualControlFlowGraphMap) {
        Map<Integer, List<ControlDependency>> nodeAndPathEdges = new HashMap<>();
        for (String key : actualControlFlowGraphMap.keySet()
        ) {
            ActualControlFlowGraph actualControlFlowGraph = actualControlFlowGraphMap.get(key);
            DefaultDirectedGraph defaultDirectedGraph = (DefaultDirectedGraph) actualControlFlowGraph.getGraph();
            Set<ControlFlowEdge> controlFlowEdges = defaultDirectedGraph.edgeSet();
            for (ControlFlowEdge controlFlowEdge : controlFlowEdges) {
                ControlDependency sourceBranch = controlFlowEdge.getControlDependency();
                if (sourceBranch != null) {
                    BytecodeInstruction lastInstruction = controlFlowEdge.getTargetData().getLastInstruction();

                    try {
                        Branch branch = branchPool.getBranchForInstruction(lastInstruction);
                        Integer targetNode = branch.getActualBranchId();
                        if (nodeAndPathEdges.containsKey(targetNode)) {
                            nodeAndPathEdges.get(targetNode).add(sourceBranch);
                        } else {
                            nodeAndPathEdges.put(targetNode, new ArrayList<>());
                            nodeAndPathEdges.get(targetNode).add(sourceBranch);
                        }
                    } catch (IllegalArgumentException e) {
                        // When target's last instruction is not a branch
                    }

                }
            }
        }
        StringBuilder sb = new StringBuilder("-------------------------------------------\nTest Suite CFG: \n");
        nodeAndPathEdges.forEach((targetNode, controlDependencies) -> {
            List<String> edgeSource = controlDependencies.stream()
                    .map(cd -> cd.getBranch().getActualBranchId() + "-" + cd.getBranchExpressionValue())
                    .collect(Collectors.toList());
            edgeSource.forEach(es -> sb.append(targetNode + " <-- " + es + "\n"));
        });
//        appendToFile("EdgeInfo.txt", sb.toString());
        return nodeAndPathEdges;
    }


    private Set<Integer> getAllPathNodes(Integer key, Map<Integer, List<ControlDependency>> nodeAndPathMap) {
        Set<Integer> results = new HashSet<>();

        Stack<Integer> stack = new Stack<>();
        stack.push(key);
        while (!stack.isEmpty()) {
            Integer pop = stack.pop();
            results.add(pop);

            if (nodeAndPathMap.containsKey(pop)) {
                List<ControlDependency> dependentNodes = nodeAndPathMap.get(pop);
                Set<Integer> appendList = dependentNodes.stream().map(cd -> cd.getBranch().getActualBranchId())
                        .filter(node -> !stack.contains(node))
                        .collect(Collectors.toSet());
                stack.addAll(appendList);
            }
        }
        return results;
    }

    private static void appendToFile(String fileName, String content) {
        BufferedWriter writer = null;
        try {
            File file = new File(fileName);

            // Create the file if it does not exist
            if (!file.exists()) {
                file.createNewFile();
            }

            // Open the file in append mode
            FileWriter fileWriter = new FileWriter(file, true);
            writer = new BufferedWriter(fileWriter);

            // Write content to file
            writer.write(content);
            writer.newLine();

        } catch (IOException e) {
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
            }
        }
    }

    private void setupBranchDC() {
        logger.info("Reduce space : " + Properties.REDUCE_SPACE);
        if (Properties.PROPOSED_DC) {
            logger.info("Proposed DC : " + Properties.PROPOSED_DC);
            if (Properties.NEXT_DATE_DC) {
                branchDifficultyCoefficient = Properties.NEXT_DATE_DIFFICULTY_COEFFICIENT_MAP;
            } else if (Properties.VALID_DATE_DC) {
                branchDifficultyCoefficient = Properties.VALID_DATE_DIFFICULTY_COEFFICIENT_MAP;
            } else if (Properties.ADD_DATE_DC) {
                branchDifficultyCoefficient = Properties.ADD_DATE_DIFFICULTY_COEFFICIENT_MAP;
            } else if (Properties.GAMMQ_DC) {
                branchDifficultyCoefficient = Properties.GAMMQ_DIFFICULTY_COEFFICIENT_MAP;
            } else if (Properties.BESSJ_DC) {
                branchDifficultyCoefficient = Properties.BESSJ_DIFFICULTY_COEFFICIENT_MAP;
            } else if (Properties.EXPINT_DC) {
                branchDifficultyCoefficient = Properties.EXPINT_DIFFICULTY_COEFFICIENT_MAP;
            } else if (Properties.BESSI_DC) {
                branchDifficultyCoefficient = Properties.BESSI_DIFFICULTY_COEFFICIENT_MAP;
            } else if (Properties.EI_DC) {
                branchDifficultyCoefficient = Properties.EI_DIFFICULTY_COEFFICIENT_MAP;
            } else if (Properties.PLGNDR_DC) {
                branchDifficultyCoefficient = Properties.PLGNDR_DIFFICULTY_COEFFICIENT_MAP;
            } else if (Properties.BETAI_DC) {
                branchDifficultyCoefficient = Properties.BETAI_DIFFICULTY_COEFFICIENT_MAP;
            } else if (Properties.RC_DC) {
                branchDifficultyCoefficient = Properties.RC_DIFFICULTY_COEFFICIENT_MAP;
            } else {
                branchDifficultyCoefficient = new HashMap<>();
            }
        }
        if (Properties.SAKTI_DC) {
            logger.info("Sakti DC : " + Properties.SAKTI_DC);
            if (Properties.NEXT_DATE_DC) {
                branchDifficultyCoefficient = Properties.SAKTI_NEXT_DATE_DIFFICULTY_COEFFICIENT_MAP;
            } else if (Properties.VALID_DATE_DC) {
                branchDifficultyCoefficient = Properties.SAKTI_VALID_DATE_DIFFICULTY_COEFFICIENT_MAP;
            } else if (Properties.ADD_DATE_DC) {
                branchDifficultyCoefficient = Properties.SAKTI_ADD_DATE_DIFFICULTY_COEFFICIENT_MAP;
            } else if (Properties.GAMMQ_DC) {
                branchDifficultyCoefficient = Properties.SAKTI_GAMMQ_DIFFICULTY_COEFFICIENT_MAP;
            } else if (Properties.BESSJ_DC) {
                branchDifficultyCoefficient = Properties.SAKTI_BESSJ_DIFFICULTY_COEFFICIENT_MAP;
            } else if (Properties.EXPINT_DC) {
                branchDifficultyCoefficient = Properties.SAKTI_EXPINT_DIFFICULTY_COEFFICIENT_MAP;
            } else if (Properties.BESSI_DC) {
                branchDifficultyCoefficient = Properties.SAKTI_BESSI_DIFFICULTY_COEFFICIENT_MAP;
            } else if (Properties.EI_DC) {
                branchDifficultyCoefficient = Properties.SAKTI_EI_DIFFICULTY_COEFFICIENT_MAP;
            } else if (Properties.PLGNDR_DC) {
                branchDifficultyCoefficient = Properties.SAKTI_PLGNDR_DIFFICULTY_COEFFICIENT_MAP;
            } else if (Properties.BETAI_DC) {
                branchDifficultyCoefficient = Properties.SAKTI_BETAI_DIFFICULTY_COEFFICIENT_MAP;
            } else if (Properties.RC_DC) {
                branchDifficultyCoefficient = Properties.SAKTI_RC_DIFFICULTY_COEFFICIENT_MAP;
            } else {
                branchDifficultyCoefficient = new HashMap<>();
            }
        }
    }
}

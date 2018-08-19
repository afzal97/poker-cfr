package com.azurefractal;

import java.util.Arrays;

public class Node {
    //infoset is characterized as cards and history, e.g. "1p" or "3pb"
    String infoSet;
    boolean[] validActions;
    int numValidActions;
    double[] regretSum = new double[Trainer.NUM_ACTIONS],
            strategy = new double[Trainer.NUM_ACTIONS],
            strategySum = new double[Trainer.NUM_ACTIONS],
            values = new double[Trainer.NUM_ACTIONS];
    Node parent_node;
    Node child_node;
    boolean is_terminal = false;
    double[][] reach_prob = new double[Trainer.NUM_CARDS][Trainer.NUM_CARDS];

    Node(boolean[] validActions, String infoSet) {
        this.validActions = validActions;
        this.infoSet = infoSet;
        for (int a = 0; a < Trainer.NUM_ACTIONS; a++) {
            if (this.validActions[a]) {
                this.numValidActions += 1;
            }
        }
    }

    //Returns strategy stored by node
    public double[] getStrategy(double realizationWeight) {
        double normalizingSum = 0;
        //For each action, take the strategy weight to be the regret sum if the regret sum is positive. Calculate normalizing sum appropriately.
        for (int a = 0; a < Trainer.NUM_ACTIONS; a++) {
            if (this.validActions[a]) {
                strategy[a] = regretSum[a] > 0 ? regretSum[a] : 0;
                normalizingSum += strategy[a];
            }
        }
        //For each action, (if normalizing sum is more than zero, normalize the strategies. Else, set all actions to equal prob).
        //Add the strategy to the strategySum, weighting by realization weight
        for (int a = 0; a < Trainer.NUM_ACTIONS; a++) {
            if (this.validActions[a]) {
                if (normalizingSum > 0)
                    strategy[a] /= normalizingSum;
                else
                    strategy[a] = 1.0 / this.numValidActions;
                strategySum[a] += realizationWeight * strategy[a];
            }
        }
        return strategy;
    }

    //Returns average strategy stored by node
    public double[] getAverageStrategy() {
        double[] avgStrategy = new double[Trainer.NUM_ACTIONS];
        double normalizingSum = 0;
        //Calculate normalizing sum. Then, normalize each action and return it. If normalization sum is non-positive, simply return uniform strategy.
        for (int a = 0; a < Trainer.NUM_ACTIONS; a++) {
            if (this.validActions[a]) {
                normalizingSum += strategySum[a];
            }
        }

        for (int a = 0; a < Trainer.NUM_ACTIONS; a++)
            if (normalizingSum > 0 && this.validActions[a]) {
                avgStrategy[a] = strategySum[a] / normalizingSum;
            } else if (this.validActions[a]) {
                avgStrategy[a] = 1.0 / this.numValidActions;
            }

        return avgStrategy;
    }

    public double[] getActualStrategy() {
        return getAverageStrategy();
//            return getStrategy(0);
    }

    //Return average strategy
    public String toString() {
        return String.format("%4s: %s", infoSet, Arrays.toString(getActualStrategy()));
    }

}
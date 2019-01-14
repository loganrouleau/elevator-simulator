package simulator;

import static simulator.Simulation.CAPACITY;
import static simulator.Simulation.FLOORS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class LookupTable {

    private Map<ActionState, Double> lut;

    public Double get(ActionState key) {
        return lut.get(binWaitTimes(key));
    }

    public void put(ActionState key, Double value) {
        lut.put(binWaitTimes(key), value);
    }

    private ActionState binWaitTimes(ActionState actionState) {
        Map<Integer, Integer> peopleWaiting = actionState.getPeopleWaiting();
        Map<Integer, Integer> waiting = new HashMap<>();
        waiting.put(0, 0);
        for (int floor = 1; floor <= FLOORS; floor++) {
            if (peopleWaiting.get(floor) > 0 && peopleWaiting.get(floor) <= 25) {
                waiting.put(floor, 25);
            } else if (peopleWaiting.get(floor) > 25) {
                waiting.put(floor, 9999);
            } else {
                waiting.put(floor, 0);
            }
        }
        return new ActionState(actionState.getAction(), actionState.getPosition(), waiting, actionState.getPeopleInElevator());
    }

    public Collection<Double> values() {
        return lut.values();
    }

    public void initialize() {
        lut = new HashMap<>();

        List<Map<Integer, Integer>> possiblePeopleWaitingCombinations = new ArrayList<>();
        int[] combinations = new int[]{0, 25, 9999};
        for (int i : combinations) {
            for (int j : combinations) {
                for (int k : combinations) {
                    for (int l : combinations) {
                        Map<Integer, Integer> peopleWaiting = new HashMap<>();
                        peopleWaiting.put(0, 0);
                        peopleWaiting.put(1, i);
                        peopleWaiting.put(2, j);
                        peopleWaiting.put(3, k);
                        peopleWaiting.put(4, l);

                        possiblePeopleWaitingCombinations.add(peopleWaiting);
                    }
                }
            }
        }

        for (ActionState.Action action : ActionState.Action.values()) {
            for (double position = 0; position <= FLOORS; position += 1) {
                for (Map<Integer, Integer> peopleWaiting : possiblePeopleWaitingCombinations) {
                    for (int peopleInElevator = 0; peopleInElevator <= CAPACITY; peopleInElevator++) {
                        ActionState actionState = new ActionState(action, position, peopleWaiting, peopleInElevator);
                        Double qValue = 0.01 * ThreadLocalRandom.current().nextDouble();
                        lut.put(actionState, qValue);
                    }
                }
            }
        }
    }
}

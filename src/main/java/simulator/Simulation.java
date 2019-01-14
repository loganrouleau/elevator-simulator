package simulator;

import static simulator.ActionState.Action.DOWN;
import static simulator.ActionState.Action.STOP;
import static simulator.ActionState.Action.UP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import javafx.scene.paint.Color;

public class Simulation implements Runnable {
    public static final int FLOORS = 4;
    public static final int CAPACITY = 3;
    public static final double ALPHA = 0.01;
    public static final double EPSILON = 0.15;
    public static final double GAMMA = 0.9;
    public static final double TIME_STEP = 0.5; // seconds
    public static final double END_TIME = 600000000; // seconds
    public static final double FLOORS_PER_TIME_STEP = 0.25;

    private ActionState actionState;
    private LookupTable lookupTable = new LookupTable();
    private int tableUpdates = 0;
    private int actionStatesVisited = 0;
    private int totalStates = 0;

    private final GuiLauncher gui;

    public Simulation(GuiLauncher gui) {
        this.gui = gui;
    }

    public void run() {
        initializeActionState();
        lookupTable.initialize();
        int cumulativeReward = 0;
        List<Integer> cumulativeRewardList = new ArrayList<>();

        for (double time = 0; time < END_TIME; time += TIME_STEP) {
            ActionState prevActionState = actionState;
            performAction();

            double reward = calculateReward();
            cumulativeReward += reward;
            cumulativeRewardList.add(cumulativeReward);

            updateWaitTimers();
            if (ThreadLocalRandom.current().nextDouble() > 0.8) {
                newPersonArrives();
            }

            List<ActionState.Action> possibleActions = getPossibleActions();
            int numberOfPossibleActions = possibleActions.size();
            ActionState.Action action = STOP; // Need to initialize, but should ALWAYS be overwritten
            ActionState actionStateCopy = actionState;

            double qMax = -Double.MAX_VALUE;
            for (ActionState.Action possibleAction : possibleActions) {
                actionStateCopy.setAction(possibleAction);
                if (lookupTable.get(actionStateCopy) > qMax) {
                    qMax = lookupTable.get(actionStateCopy);
                    action = possibleAction;
                }
            }

            if (numberOfPossibleActions > 0) {
                if (!gui.isTrainingActive() || ThreadLocalRandom.current().nextDouble() > EPSILON) {
                    // greedy action (the action was already calculated above)
                    gui.setElevatorColour(Color.GREEN);
                } else {
                    // random action
                    action = possibleActions.get(ThreadLocalRandom.current().nextInt(numberOfPossibleActions));
                    gui.setElevatorColour(Color.RED);
                }
                actionState.setAction(action);

                System.out.println(actionState);

                if (gui.isTrainingActive()) {
                    double prevQValue = lookupTable.get(prevActionState);
                    lookupTable.put(prevActionState, (1 - ALPHA) * prevQValue + ALPHA * (reward + GAMMA * qMax));
                    tableUpdates++;
                }

                if (tableUpdates % 5 == 1) {
                    Collection<Double> values = lookupTable.values();
                    totalStates = values.size();
                    actionStatesVisited = (int) values.stream().filter(q -> q < 0).count();
                }
                gui.setInfoText("tableUpdates: " + tableUpdates + "\nstatesVisited: " + actionStatesVisited + "/" + totalStates);
            }

            try {
                Thread.sleep(gui.getIterationDelayMillis() + 30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (int i : cumulativeRewardList) {
            System.out.println(i);
        }
    }

    private double calculateReward() {
        double reward = 0;
        for (int i = 1; i <= FLOORS; i++) {
            reward -= Math.pow(actionState.getPeopleWaiting().get(i), 2);
        }
        return reward;
    }

    private void performAction() {
        double position = actionState.getPosition();
        switch (actionState.getAction()) {
            case UP:
                actionState.setPosition(position + FLOORS_PER_TIME_STEP);
                gui.moveUp();
                break;
            case DOWN:
                actionState.setPosition(position - FLOORS_PER_TIME_STEP);
                gui.moveDown();
                break;
            case STOP:
                int currentFloor = (int) actionState.getPosition();
                if (currentFloor == 0 && actionState.getPeopleInElevator() > 0) {
                    // people get off the elevator
                    actionState.setPeopleInElevator(0);
                    gui.updatePeopleInElevator(0);
                }
                if (actionState.getPeopleWaiting().get(currentFloor) > 0) {
                    if (actionState.getPeopleInElevator() == CAPACITY) {
                        break;
                    }
                    // person gets on the elevator
                    Map<Integer, Integer> peopleWaiting = actionState.getPeopleWaiting();
                    peopleWaiting.put(currentFloor, 0);
                    actionState.setPeopleWaiting(peopleWaiting);
                    gui.updatePeopleWaiting(peopleWaiting);
                    actionState.setPeopleInElevator(actionState.getPeopleInElevator() + 1);
                    gui.updatePeopleInElevator(actionState.getPeopleInElevator());
                }
                break;
        }
    }

    private void newPersonArrives() {
        Map<Integer, Integer> peopleWaiting = actionState.getPeopleWaiting();
        int floorToUpdate = ThreadLocalRandom.current().nextInt(1, FLOORS + 1);
        if (peopleWaiting.get(floorToUpdate) == 0) {
            peopleWaiting.put(floorToUpdate, 1);
        }
        actionState.setPeopleWaiting(peopleWaiting);
        gui.updatePeopleWaiting(peopleWaiting);
    }

    private void updateWaitTimers() {
        Map<Integer, Integer> peopleWaiting = actionState.getPeopleWaiting();
        for (int floor = 1; floor <= FLOORS; floor++) {
            if (peopleWaiting.get(floor) > 0) {
                peopleWaiting.put(floor, peopleWaiting.get(floor) + 1);
            }
        }
        actionState.setPeopleWaiting(peopleWaiting);
        gui.updatePeopleWaiting(peopleWaiting);
    }

    private List<ActionState.Action> getPossibleActions() {
        if (actionState.getPosition() % 1 != 0) {
            return Collections.emptyList();
        }
        int currentFloor = (int) actionState.getPosition();
        int peopleInElevator = actionState.getPeopleInElevator();
        ActionState.Action prevAction = actionState.getAction();

        if (currentFloor == 0) {
            if (prevAction == DOWN) {
                return Collections.singletonList(STOP);
            } else {
                return Arrays.asList(UP, STOP);
            }
        } else if (currentFloor == FLOORS) {
            if (prevAction == UP) {
                return Collections.singletonList(STOP);
            } else {
                return Arrays.asList(DOWN, STOP);
            }
        } else {
            if (peopleInElevator == CAPACITY) {
                return Collections.singletonList(DOWN);
            } else if (peopleInElevator > 0 && actionState.getPeopleWaiting().get(currentFloor) == 0) {
                return Collections.singletonList(DOWN);
            } else if (peopleInElevator > 0 && actionState.getPeopleWaiting().get(currentFloor) > 0) {
                return Arrays.asList(DOWN, STOP);
            } else if (prevAction == UP) {
                return Arrays.asList(UP, STOP);
            } else if (prevAction == DOWN) {
                return Arrays.asList(DOWN, STOP);
            } else {
                return Arrays.asList(UP, DOWN, STOP);
            }
        }
    }

    private void initializeActionState() {
        Map<Integer, Integer> peopleWaiting = new HashMap<>();
        peopleWaiting.put(0, 0);
        peopleWaiting.put(1, 0);
        peopleWaiting.put(2, 0);
        peopleWaiting.put(3, 0);
        peopleWaiting.put(4, 0);
        actionState = new ActionState(STOP, 0, peopleWaiting, 0);
    }

}

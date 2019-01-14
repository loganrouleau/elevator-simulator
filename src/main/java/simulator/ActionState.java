package simulator;

import java.util.Map;

public class ActionState {

    public enum Action {
        UP,
        DOWN,
        STOP
    }

    private Action action;
    private double position;
    private Map<Integer, Integer> peopleWaiting;
    private int peopleInElevator;

    public ActionState(Action action, double position, Map<Integer, Integer> peopleWaiting, int peopleInElevator) {
        this.action = action;
        this.position = position;
        this.peopleWaiting = peopleWaiting;
        this.peopleInElevator = peopleInElevator;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public double getPosition() {
        return position;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    public Map<Integer, Integer> getPeopleWaiting() {
        return peopleWaiting;
    }

    public void setPeopleWaiting(Map<Integer, Integer> peopleWaiting) {
        this.peopleWaiting = peopleWaiting;
    }

    public int getPeopleInElevator() {
        return peopleInElevator;
    }

    public void setPeopleInElevator(int peopleInElevator) {
        this.peopleInElevator = peopleInElevator;
    }

    @Override
    public String toString() {
        return String.format("ActionState {position=%.2f peopleWaiting=[%d, %d, %d, %d] peopleInElevator=%d action=%s}",
                position, peopleWaiting.get(1), peopleWaiting.get(2), peopleWaiting.get(3), peopleWaiting.get(4), peopleInElevator, action);
    }

 /*   @Override
    public int compareTo(ActionState o) {
        if (((Double) this.position).compareTo(o.position) == 0) {
            if (((Integer) this.peopleInElevator).compareTo(o.peopleInElevator) == 0) {
                return 1;
            } else {
                return ((Integer) this.peopleInElevator).compareTo(o.peopleInElevator);
            }
        }
        return ((Double) this.position).compareTo(o.position);
    }
*/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActionState that = (ActionState) o;

        if (Double.compare(that.getPosition(), getPosition()) != 0) return false;
        if (getPeopleInElevator() != that.getPeopleInElevator()) return false;
        if (getAction() != that.getAction()) return false;
        for (int key = 1; key <= 4; key++) {
            if (getPeopleWaiting().get(key) == 0 && that.getPeopleWaiting().get(key) != 0) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = getAction().hashCode();
        temp = Double.doubleToLongBits(getPosition());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (getPeopleWaiting().get(1));
        result = 31 * result + (getPeopleWaiting().get(2));
        result = 31 * result + (getPeopleWaiting().get(3));
        result = 31 * result + (getPeopleWaiting().get(4));
        result = 31 * result + getPeopleInElevator();
        return result;
    }
}

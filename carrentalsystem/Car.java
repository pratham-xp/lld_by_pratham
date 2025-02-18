package carrentalsystem;

import java.util.concurrent.atomic.AtomicReference;
import carrentalsystem.State.*;

public class Car {
    private final String id;
    private final CarType type;
    private final AtomicReference<CarState> state = new AtomicReference<>(new AvailableState());
    private volatile Station currentStation;

    public Car(String id, CarType type, Station station) {
        this.id = id;
        this.type = type;
        this.currentStation = station;
        station.addCar(this);
    }

    public boolean book() {
        CarState currentState = state.get();
        if (currentState instanceof AvailableState) {
            if (state.compareAndSet(currentState, new BookedState())) {
                currentStation.removeCar(this);
                currentStation = null;
                return true;
            }
        }
        return false;
    }

    public void returnToStation(Station station) {
        CarState currentState = state.get();
        currentState.returnToStation(this, station);
    }

    public void repair() {
        CarState currentState = state.get();
        currentState.repair(this);
    }

    public void setState(CarState state) {
        this.state.set(state);
    }

    public CarType getType() { return type; }
    public Station getCurrentStation() { return currentStation; }
}
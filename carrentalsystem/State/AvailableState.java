package carrentalsystem.State;

import carrentalsystem.Car;
import carrentalsystem.Station;

public class AvailableState implements CarState {
    @Override
    public void book(Car car) {
        car.setState(new BookedState());
    }

    @Override
    public void returnToStation(Car car, Station station) {
        throw new IllegalStateException("Car already available");
    }

    @Override
    public void repair(Car car) {
        car.setState(new NeedRepairState());
    }
}

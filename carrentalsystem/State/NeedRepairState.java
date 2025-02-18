package carrentalsystem.State;

import carrentalsystem.Car;
import carrentalsystem.Station;

public class NeedRepairState implements CarState {
    @Override
    public void book(Car car) {
        throw new IllegalStateException("Car needs repair");
    }

    @Override
    public void returnToStation(Car car, Station station) {
        throw new IllegalStateException("Car needs repair");
    }

    @Override
    public void repair(Car car) {
        car.setState(new AvailableState());
    }
}
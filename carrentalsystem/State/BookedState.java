package carrentalsystem.State;

import carrentalsystem.*;

public class BookedState implements CarState {
    @Override
    public void book(Car car) {
        throw new IllegalStateException("Car already booked");
    }

    @Override
    public void returnToStation(Car car, Station station) {
        car.setState(new AvailableState());
        station.addCar(car);
    }

    @Override
    public void repair(Car car) {
        throw new IllegalStateException("Car is booked");
    }
}

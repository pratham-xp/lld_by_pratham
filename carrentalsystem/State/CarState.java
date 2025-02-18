package carrentalsystem.State;

import carrentalsystem.Car;
import carrentalsystem.Station;

public interface CarState {
    void book(Car car);
    void returnToStation(Car car, Station station);
    void repair(Car car);
}
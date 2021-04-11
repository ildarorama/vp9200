package sample;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

public interface PortRunner {
    void accept(SerialPort port) throws SerialPortException, SerialPortTimeoutException;
}

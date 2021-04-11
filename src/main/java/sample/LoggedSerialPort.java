package sample;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;
import org.zeromq.ZMQ;

import java.util.function.Consumer;

public class LoggedSerialPort extends SerialPort {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private Consumer<String> logConsumer;

    private ZMQ.Socket socket = null;

    public LoggedSerialPort(String portName, Consumer<String> log) {
        super(portName);
        this.logConsumer = log;
    }

    public LoggedSerialPort(ZMQ.Socket socket, Consumer<String> log) {
        super("");
        this.socket = socket;
        this.logConsumer = log;
    }

    public boolean writeBytes(byte[] buffer) throws SerialPortException {
        if (socket != null) {
            return socket.send(buffer);
        }
        logConsumer.accept(">" + bytesToHex(buffer) + " CRC: " + (checkCrc(buffer) ? "Ok" : "Fail"));
        return super.writeBytes(buffer);
    }

    public byte[] readBytes(int byteCount, int timeout) throws SerialPortException, SerialPortTimeoutException {
        if (socket != null) {
            return socket.recv();
        }
        byte[] result = super.readBytes(byteCount, timeout);
        String desc = bytesToHex(result);

        int descSize = desc.length() - 10;
        if (descSize > 4) {
            StringBuilder offsetString = new StringBuilder();
            offsetString.append("   Offsets   ");
            int offset = 5;
            while(offsetString.length() < descSize) {
                offsetString.append(String.format("%3d  ", offset));
                offset+=2;
            }
            logConsumer.accept(offsetString.toString());
        }
        logConsumer.accept("<" + desc + " CRC: " + (checkCrc(result) ? "Ok" : "Fail"));
        return result;
    }

    public static boolean checkCrc(byte[] data) {
        int sum = 0xFF;
        for (int i = 0; i < data.length - 1; i++) {
            sum -= data[i] & 0xFF;
        }
        sum &= 0xFF;
        int crc = data[data.length - 1] & 0xFF;
        return crc == sum;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        StringBuilder sb = new StringBuilder();
        sb.append(hexChars);

        if (sb.length() >= 16) {
            for (int i = sb.length() - 2; i >= 10; i -= 4) {
                sb.insert(i, "|");
            }
            sb.insert(sb.length() - 2, " ");
            sb.insert(10, " ");

            int packetSize= Integer.parseInt(sb.substring(6,8), 16);
            sb.append(" (" + packetSize + ")");
        }

        return sb.toString();
    }
}

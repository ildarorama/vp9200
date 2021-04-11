package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class Controller implements Initializable {

    private static final byte REQ_VERSION[] = {
            (byte) 0x0D, (byte) 0x77, (byte) 0x01, (byte) 0x05, (byte) 0x06, (byte) 0x00,
            /* req*/ (byte) 0x01, /* req*/ (byte) 0x00, (byte) 0x02, (byte) 0x6C /*crc*/
    };

    private static final byte REQ_DATE[] = {
            (byte) 0x0D, (byte) 0x77, (byte) 0x01, (byte) 0x05, (byte) 0x06, (byte) 0x00,
            /* req*/ (byte) 0x01, /* req*/ (byte) 0x18, (byte) 0x02, (byte) 0x54 /*crc*/
    };

    private static final byte REQ_LOADDATE[] = {
            (byte) 0x0D, (byte) 0x77, (byte) 0x01, (byte) 0x05, (byte) 0x06, (byte) 0x00,
            /* req*/ (byte) 0x6E, /* req*/ (byte) 0x0B, (byte) 0x02, (byte) 0xF4 /*crc*/
    };

    private static final byte REQ_LOADSTATUS[] = {
            (byte) 0x0D, (byte) 0x77, (byte) 0x01, (byte) 0x05, (byte) 0x06, (byte) 0x00,
            /* req*/ (byte) 0x16, /* req*/ (byte) 0x00, (byte) 0x00, (byte) 0x59 /*crc*/
    };

    private static final byte REQ_HEAT1SUMMARY[] = {
            (byte) 0x0D, (byte) 0x77, (byte) 0x01, (byte) 0x05, (byte) 0x06, (byte) 0x00,
            /* req*/ (byte) 0x3B, /* req*/ (byte) 0x07, (byte) 0x0A, (byte) 0x23 /*crc*/
    };

    private static final byte REQ_HEAT2SUMMARY[] = {
            (byte) 0x0D, (byte) 0x77, (byte) 0x01, (byte) 0x05, (byte) 0x06, (byte) 0x00,
            /* req*/ (byte) 0x3C, /* req*/ (byte) 0x07, (byte) 0x0A, (byte) 0x22 /*crc*/
    };

    private static final byte REQ_HEAT3SUMMARY[] = {
            (byte) 0x0D, (byte) 0x77, (byte) 0x01, (byte) 0x05, (byte) 0x06, (byte) 0x00,
            /* req*/ (byte) 0x3D, /* req*/ (byte) 0x07, (byte) 0x0A, (byte) 0x21 /*crc*/
    };

    private static final byte REQ_HEAT1STATUS[] = {
            (byte) 0x0D, (byte) 0x77, (byte) 0x01, (byte) 0x05, (byte) 0x06, (byte) 0x00,
            /* req*/ (byte) 0x7C, /* req*/ (byte) 0x00, (byte) 0x01, (byte) 0xF2 /*crc*/
    };

    private static final byte REQ_HEAT2STATUS[] = {
            (byte) 0x0D, (byte) 0x77, (byte) 0x01, (byte) 0x05, (byte) 0x06, (byte) 0x00,
            /* req*/ (byte) 0x7D, /* req*/ (byte) 0x00, (byte) 0x01, (byte) 0xF1 /*crc*/
    };

    private static final byte REQ_HEAT3STATUS[] = {
            (byte) 0x0D, (byte) 0x77, (byte) 0x01, (byte) 0x05, (byte) 0x06, (byte) 0x00,
            /* req*/ (byte) 0x7E, /* req*/ (byte) 0x00, (byte) 0x01, (byte) 0xF0 /*crc*/
    };

    private static final byte REQ_OTHERSTATUS[] = {
            (byte) 0x0D, (byte) 0x77, (byte) 0x01, (byte) 0x05, (byte) 0x06, (byte) 0x00,
            /* req*/ (byte) 0x41, /* req*/ (byte) 0x07, (byte) 0x0A, (byte) 0x1D /*crc*/
    };

    private static final byte REQ_HUMIDITYSTATUS[] = {
            (byte) 0x0D, (byte) 0x77, (byte) 0x01, (byte) 0x05, (byte) 0x06, (byte) 0x00,
            /* req*/ (byte) 0x43, /* req*/ (byte) 0x07, (byte) 0x0A, (byte) 0x1B /*crc*/
    };

    private static final byte REQ_COOLSTATUS1[] = {
            (byte) 0x0D, (byte) 0x77, (byte) 0x01, (byte) 0x05, (byte) 0x06, (byte) 0x00,
            /* req*/ (byte) 0x16, /* req*/ (byte) 0x00, (byte) 0x00, (byte) 0x59 /*crc*/
    }; //216 length of answer

    private static final byte REQ_COOLSTATUS2[] = {
            (byte) 0x0D, (byte) 0x77, (byte) 0x01, (byte) 0x05, (byte) 0x06, (byte) 0x00,
            /* req*/ (byte) 0x17, /* req*/ (byte) 0x00, (byte) 0x00, (byte) 0x58 /*crc*/
    }; //188 length of answer

    private static final byte REQ_COOLSTATUS3[] = {
            (byte) 0x0D, (byte) 0x77, (byte) 0x01, (byte) 0x05, (byte) 0x06, (byte) 0x00,
            /* req*/ (byte) 0x18, /* req*/ (byte) 0x00, (byte) 0x31, (byte) 0x26 /*crc*/
    }; //104 length of answer

    private static final byte REQ_RIGHTWNDSTATUS1[] = {
            (byte) 0x0D, (byte) 0x77, (byte) 0x01, (byte) 0x05, (byte) 0x06, (byte) 0x00,
            /* req*/ (byte) 0x1F, /* req*/ (byte) 0x00, (byte) 0x00, (byte) 0x50 /*crc*/
    }; //110 length of answer

    private static final byte REQ_RIGHTWNDSTATUS2[] = {
            (byte) 0x0D, (byte) 0x77, (byte) 0x01, (byte) 0x05, (byte) 0x06, (byte) 0x00,
            /* req*/ (byte) 0x75, /* req*/ (byte) 0x00, (byte) 0x00, (byte) 0xFA /*crc*/
    }; //102 length of answer

    private static final byte REQ_RIGHTWNDSTATUS3[] = {
            (byte) 0x0D, (byte) 0x77, (byte) 0x01, (byte) 0x05, (byte) 0x06, (byte) 0x00,
            /* req*/ (byte) 0x20, /* req*/ (byte) 0x00, (byte) 0x00, (byte) 0x4F /*crc*/
    }; //42 length of answer


    private static final byte REQ_LEFTWNDSTATUS1[] = {
            (byte) 0x0D, (byte) 0x77, (byte) 0x01, (byte) 0x05, (byte) 0x06, (byte) 0x00,
            /* req*/ (byte) 0x1B, /* req*/ (byte) 0x00, (byte) 0x00, (byte) 0x54 /*crc*/
    }; //110 length of answer

    private static final byte REQ_LEFTWNDSTATUS2[] = {
            (byte) 0x0D, (byte) 0x77, (byte) 0x01, (byte) 0x05, (byte) 0x06, (byte) 0x00,
            /* req*/ (byte) 0x74, /* req*/ (byte) 0x00, (byte) 0x00, (byte) 0xFB /*crc*/
    }; //102 length of answer

    private static final byte REQ_LEFTWNDSTATUS3[] = {
            (byte) 0x0D, (byte) 0x77, (byte) 0x01, (byte) 0x05, (byte) 0x06, (byte) 0x00,
            /* req*/ (byte) 0x1C, /* req*/ (byte) 0x00, (byte) 0x00, (byte) 0x53 /*crc*/
    }; //42 length of answer

    @FXML
    private ComboBox<String> cbxPort;

    @FXML
    private ListView<String> lstResult;

    @FXML
    private ListView<String> lstLog;

    @FXML
    private TextField edtPattern;

    @FXML
    private TextField edtOffset;

    @FXML
    private TextField edtAddress;

    @FXML
    private TextField edtSize;

    @FXML
    private TextField edtValue;

    private ZMQ.Socket zmqMon = null;
    private ZMQ.Socket zmqReq = null;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<String> ports = Arrays.asList(SerialPortList.getPortNames());
        cbxPort.getItems().addAll(ports);
        cbxPort.getItems().add("ZMQ");
        lstLog.getItems().add("Starting app....");

        Consumer<String> cons =  (log) ->
            Platform.runLater(() -> {
                int logSize = lstLog.getItems().size();
                lstLog.getItems().add(log);
                lstLog.scrollTo(logSize);
            });

        ZContext context = new ZContext(2);
        zmqMon = context.createSocket(ZMQ.SUB);
        zmqReq = context.createSocket(ZMQ.REQ);
        zmqMon.connect("tcp://127.0.0.1:5556");
        zmqReq.connect("tcp://127.0.0.1:5555");
        zmqMon.subscribe("");
        Thread t = new Thread(() -> {
            while(true) {
                byte packet[] = zmqMon.recv();
                String desc = LoggedSerialPort.bytesToHex(packet);

                int descSize = desc.length() - 10;
                if (descSize > 4) {
                    StringBuilder offsetString = new StringBuilder();
                    offsetString.append("   Offsets   ");
                    int offset = 5;
                    while (offsetString.length() < descSize) {
                        offsetString.append(String.format("%3d  ", offset));
                        offset += 2;
                    }
                    cons.accept(offsetString.toString());
                }
                cons.accept("<" + desc + " CRC: " + (LoggedSerialPort.checkCrc(packet) ? "Ok" : "Fail"));
            }
        });
        t.setDaemon(true);
        t.setName("Mon");
        t.start();

    }

    @FXML
    public void doVersion(ActionEvent e) {
        runOnPort((port) -> {
            port.writeBytes(REQ_VERSION);
            byte[] result = port.readBytes(10, 1000);
            String desc = describePackage(REQ_VERSION, result);
            lstResult.getItems().add(desc);
        });
    }
    public static byte calcCrc(byte[] data) {
        int sum = 0xFF;
        for (int i = 0; i < data.length - 1; i++) {
            sum -= data[i] & 0xFF;
        }
        sum &= 0xFF;
        return (byte) sum;
    }

    @FXML
    public void doCustom(ActionEvent e) {
        int addr = Integer.parseInt(edtAddress.getText(), 16);
        int size = Integer.parseInt(edtSize.getText());
        byte pack[] = {
                (byte) 0x0D, (byte) 0x77, (byte) 0x01, (byte) 0x05, (byte) 0x06, (byte) 0x00,
                /* req*/ (byte) 0x01, /* req*/ (byte) 0x00, (byte) 0x02, (byte) 0x6C /*crc*/
        };
        pack[8]=(byte)size;
        byte b1 = (byte) (addr & 0xFF);
        byte b2 = (byte) ((addr >> 8) & 0xFF);
        pack[7] = b1;
        pack[6] = b2;
        pack[9] = calcCrc(pack);
        runOnPort((port) -> {
            port.writeBytes(pack);
            port.readBytes(10, 1000);
        });
    }

            @FXML
    public void doDate(ActionEvent e) {
        runOnPort((port) -> {
            port.writeBytes(REQ_DATE);
            byte[] result = port.readBytes(10, 1000);
            String desc = describePackage(REQ_DATE, result);
            lstResult.getItems().add(desc);
        });
    }

    @FXML
    public void doLoadDate(ActionEvent e) {
        runOnPort((port) -> {
            port.writeBytes(REQ_LOADDATE);
            byte[] result = port.readBytes(10, 1000);
            String desc = describePackage(REQ_LOADDATE, result);
            lstResult.getItems().add(desc);
        });
    }

    @FXML
    public void doLoadStatus(ActionEvent e) {
        runOnPort((port) -> {
            port.writeBytes(REQ_LOADSTATUS);
            byte[] result = port.readBytes(216, 1000);
            String desc = describePackage(REQ_LOADSTATUS, result);
            lstResult.getItems().add(desc);
        });
    }

    @FXML
    public void doHeat1Status(ActionEvent e) {
        runOnPort((port) -> {
            port.writeBytes(REQ_HEAT1STATUS);
            byte[] result = port.readBytes(8, 1000);
            String desc = describePackage(REQ_HEAT1STATUS, result);
            lstResult.getItems().add(desc);

            port.writeBytes(REQ_HEAT1SUMMARY);
            result = port.readBytes(26, 1000);
            desc = describePackage(REQ_HEAT1SUMMARY, result);
            lstResult.getItems().add(desc);
        });
    }

    @FXML
    public void doHeat2Status(ActionEvent e) {
        runOnPort((port) -> {
            port.writeBytes(REQ_HEAT2STATUS);
            byte[] result = port.readBytes(8, 1000);
            String desc = describePackage(REQ_HEAT2STATUS, result);
            lstResult.getItems().add(desc);

            port.writeBytes(REQ_HEAT2SUMMARY);
            result = port.readBytes(26, 1000);
            desc = describePackage(REQ_HEAT2SUMMARY, result);
            lstResult.getItems().add(desc);
        });
    }

    @FXML
    public void doHeat3Status(ActionEvent e) {
        runOnPort((port) -> {
            port.writeBytes(REQ_HEAT3STATUS);
            byte[] result = port.readBytes(8, 1000);
            String desc = describePackage(REQ_HEAT3STATUS, result);
            lstResult.getItems().add(desc);

            port.writeBytes(REQ_HEAT3SUMMARY);
            result = port.readBytes(26, 1000);
            desc = describePackage(REQ_HEAT3SUMMARY, result);
            lstResult.getItems().add(desc);
        });
    }

    @FXML
    public void doOtherStatus(ActionEvent e) {
        runOnPort((port) -> {
            port.writeBytes(REQ_OTHERSTATUS);
            byte[] result = port.readBytes(26, 1000);
            String desc = describePackage(REQ_OTHERSTATUS, result);
            lstResult.getItems().add(desc);
        });
    }

    @FXML
    public void doHumidityStatus(ActionEvent e) {
        runOnPort((port) -> {
            port.writeBytes(REQ_HUMIDITYSTATUS);
            byte[] result = port.readBytes(26, 1000);
            String desc = describePackage(REQ_HUMIDITYSTATUS, result);
            lstResult.getItems().add(desc);
        });
    }


    @FXML
    public void doClear(ActionEvent e) {
        lstResult.getItems().clear();
        lstLog.getItems().clear();
    }

    @FXML
    public void doPattern(ActionEvent e) {
        byte offset = Byte.parseByte(edtOffset.getText());

        ByteBuffer bb = ByteBuffer.allocate(6);
        bb.put(0, (byte)-1);
        bb.put(1, offset);

        int i = Integer.parseInt(edtPattern.getText(), 16);
//        int j = i & 0xFF00;
//        j = j >> 8;
//        i = i << 8;
//        i = i | j;
        short s = (short) (i & 0xFFFF);

        bb.putShort(2, s);

        i = Integer.parseInt(edtValue.getText(), 16);
//        j = i & 0xFF00;
//        j = j >> 8;
//        i = i << 8;
//        i = i | j;
        s = (short) (i & 0xFFFF);
        bb.putShort(4, s);

        zmqReq.send(bb.array());
        zmqReq.recv();
    }

    @FXML
    public void doCoolStatus(ActionEvent e) {
        runOnPort((port) -> {
            port.writeBytes(REQ_COOLSTATUS1);
            byte[] result1 = port.readBytes(216, 1000);

            port.writeBytes(REQ_COOLSTATUS2);
            byte[] result2 = port.readBytes(188, 1000);

            port.writeBytes(REQ_COOLSTATUS3);
            byte[] result3 = port.readBytes(104, 1000);

            List<String> desc = buildCoolResponse(result1, result2, result3);

            lstResult.getItems().addAll(desc);
        });
    }

    @FXML
    public void doCoolRightStatus(ActionEvent e) {
        runOnPort((port) -> {
            port.writeBytes(REQ_RIGHTWNDSTATUS1);
            byte[] result1 = port.readBytes(110, 1000);

            port.writeBytes(REQ_RIGHTWNDSTATUS2);
            byte[] result2 = port.readBytes(102, 1000);

            port.writeBytes(REQ_RIGHTWNDSTATUS3);
            byte[] result3 = port.readBytes(42, 1000);

            List<String> desc = buildRightCoolResponse(result1, result2, result3);

            lstResult.getItems().addAll(desc);
        });
    }

    @FXML
    public void doCoolLeftStatus(ActionEvent e) {
        runOnPort((port) -> {
            port.writeBytes(REQ_LEFTWNDSTATUS1);
            byte[] result1 = port.readBytes(110, 1000);

            port.writeBytes(REQ_LEFTWNDSTATUS2);
            byte[] result2 = port.readBytes(102, 1000);

            port.writeBytes(REQ_LEFTWNDSTATUS3);
            byte[] result3 = port.readBytes(42, 1000);

            List<String> desc = buildRightCoolResponse(result1, result2, result3);

            lstResult.getItems().addAll(desc);
        });
    }

    private List<String> buildRightCoolResponse(byte[] result1, byte[] result2, byte[] result3) {
        List<String> result = new ArrayList<>();

        ByteBuffer bb1 = ByteBuffer.wrap(result1);
        ByteBuffer bb2 = ByteBuffer.wrap(result2);
        ByteBuffer bb3 = ByteBuffer.wrap(result3);

        result.add(String.format("Установки температуры: %.01f %.01f",
                bb1.getShort(25) / 100.0,
                bb1.getShort(39) / 100.0
        ));
        result.add(String.format("Диаппазон: %.02f %.02f",
                bb1.getShort(27) / 100.0,
                bb1.getShort(41) / 100.0
        ));

        result.add(String.format("Минимальная заслонка: %.02f %.02f",
                bb1.getShort(29) / 100.0,
                bb1.getShort(43) / 100.0 //hz
        ));
        result.add(String.format("Максимальная заслонка: %.02f %.02f",
                bb1.getShort(31) / 100.0,
                bb1.getShort(45) / 100.0 //hz
        ));
        result.add(String.format("Текущая температура: %.01f",
                bb3.getShort(19) / 100.0
        ));

        result.add(String.format("Расч откр клапанов: %.01f %%",
                bb2.getShort(55) / 100.0
        ));

//        result.add(String.format("Текущее открытие заслонки: %.01f %%",
//                bb3.getShort(55) / 100.0//hz
//        ));
        return result;
    }

    private List<String> buildCoolResponse(byte[] result1, byte[] result2, byte[] result3) {
        List<String> result = new ArrayList<>();

        ByteBuffer bb1 = ByteBuffer.wrap(result1);
        ByteBuffer bb2 = ByteBuffer.wrap(result2);
        ByteBuffer bb3 = ByteBuffer.wrap(result3);
        result.add(String.format("Установки температуры: %.01f %.01f",
                bb2.getShort(23) / 100.0,
                bb2.getShort(25) / 100.0
        ));
        result.add(String.format("Диаппазон: %.02f %.02f",
                bb2.getShort(27) / 100.0,
                bb2.getShort(29) / 100.0
        ));
        result.add(String.format("Текущая температура: %.01f",
                bb2.getShort(19) / 100.0
        ));
        result.add(String.format("Текущая вентиляция: %.01f",
                bb2.getShort(31) / 100.0
        ));
        result.add(String.format("Производительность: %d",
                bb2.getShort(65)
        ));

        result.add(String.format("Максимальная вентиляция: %.02f %.02f",
                bb1.getShort(27) / 100.0,
                bb1.getShort(27) / 100.0 //hz
        ));


        return result;
    }


    private String describePackage(byte[] req, byte[] resp) {
        StringBuffer sb = new StringBuffer();

        if (req == REQ_VERSION) {
            ByteBuffer bb = ByteBuffer.wrap(resp);
            sb.append(String.format("Устройство: %d Версия: %.02f",
                    bb.getShort(5),
                    bb.getShort(7) / 100.0
            ));
        }

        if (req == REQ_DATE) {
            ByteBuffer bb = ByteBuffer.wrap(resp);
            int time = bb.getShort(7);
            int hour = time / 60;
            int minute = time % 60;

            int day = bb.getShort(5);
            LocalDate date = LocalDate.of(2000, 1, 1);
            date = date.plusDays(day);

            sb.append(String.format("Время: %02d:%02d Дата: %d-%02d-%02d",
                    hour,
                    minute,
                    date.getYear(),
                    date.getMonthValue(),
                    date.getDayOfMonth()
            ));
        }

        if (req == REQ_LOADDATE) {
            ByteBuffer bb = ByteBuffer.wrap(resp);

            int day = bb.getShort(5);
            LocalDate date = LocalDate.of(2001, 1, 1);
            date = date.plusDays(day);

            sb.append(String.format("Дата загрузки: %d-%02d-%02d",
                    date.getYear(),
                    date.getMonthValue(),
                    date.getDayOfMonth()
            ));
        }

        if (req == REQ_LOADSTATUS) {
            ByteBuffer bb = ByteBuffer.wrap(resp);
            sb.append(String.format("Внутренняя температура (уст): %.02f Внутренняя температура (факт): %.02f",
                    bb.getShort(91) / 100.0,
                    bb.getShort(93) / 100.0
            ));
        }

        if (req == REQ_HEAT1STATUS) {
            ByteBuffer bb = ByteBuffer.wrap(resp);
            sb.append(String.format("Состояние нагревашки 1: %s",
                    bb.getShort(5) == 1 ? "Вкл" : "Выкл"
            ));
        }

        if (req == REQ_HEAT2STATUS) {
            ByteBuffer bb = ByteBuffer.wrap(resp);
            sb.append(String.format("Состояние нагревашки 2: %s",
                    bb.getShort(5) == 1 ? "Вкл" : "Выкл"
            ));
        }

        if (req == REQ_HEAT3STATUS) {
            ByteBuffer bb = ByteBuffer.wrap(resp);
            sb.append(String.format("Состояние нагревашки 3: %s",
                    bb.getShort(5) == 1 ? "Вкл" : "Выкл"
            ));
        }

        if (req == REQ_HEAT1SUMMARY || req == REQ_HEAT2SUMMARY || req == REQ_HEAT3SUMMARY) {
            ByteBuffer bb = ByteBuffer.wrap(resp);
            sb.append(String.format("Обогрев: %s, Установки %d, Температура (уст): %.02f Температура (факт): %.02f",
                    bb.getShort(13) == 1 ? "Вкл" : "Выкл",
                    bb.getShort(21),
                    bb.getShort(5) / 100.0,
                    bb.getShort(11) / 100.0
            ));
        }

        if (req == REQ_OTHERSTATUS) {
            ByteBuffer bb = ByteBuffer.wrap(resp);
            sb.append(String.format(
                    "Контроль давления: %s, Давление (уст): %.02f Па " +
                            "Давление (факт): %.02f Па, Текущее давление %.02f Расчетное давление %.02f %% Статус: %s",
                    bb.getShort(13) == 1 ? "Вкл" : "Выкл",
                    bb.getShort(9) / 100.0,
                    bb.getShort(11) / 100.0,
                    bb.getShort(5) / 100.0,
                    bb.getShort(21) / 100.0,
                    bb.getShort(23) == 1 ? "Вкл" : "Выкл"
            ));
        }

        if (req == REQ_HUMIDITYSTATUS) {
            ByteBuffer bb = ByteBuffer.wrap(resp);
            sb.append(String.format(
                    "Контроль давления: %s, Влажномть (уст): %.02f %% " +
                            "Влажность (факт): %.02f %%, Статус: %s",
                    bb.getShort(13) == 1 ? "Вкл" : "Выкл",
                    bb.getShort(7) / 100.0,
                    bb.getShort(5) / 100.0,
                    bb.getShort(23) == 1 ? "Вкл" : "Выкл"
            ));
        }


        return sb.toString();
    }

    private void runOnPort(PortRunner func) {
        String portName = cbxPort.getSelectionModel().getSelectedItem();
        if (portName == null || portName.isEmpty()) {
            showError("Can not read command", new NullPointerException("No port selected"));
            return;
        }

        Consumer<String> logger = (log) -> {
            int logSize = lstLog.getItems().size();
            lstLog.getItems().add(log);
            lstLog.scrollTo(logSize);
        };

        SerialPort serialPort;
        if ("ZMQ".equals(portName)) {
            serialPort = new LoggedSerialPort(zmqReq, logger);
        } else {
            serialPort = new LoggedSerialPort(portName, logger);
        }


        try {
            if (!"ZMQ".equals(portName)) {
                serialPort.openPort();
                serialPort.setParams(SerialPort.BAUDRATE_9600,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            }

            func.accept(serialPort);

            if (!"ZMQ".equals(portName)) {
                serialPort.closePort();
            }
        } catch (Exception serialPortException) {
            showError("Can not read command", serialPortException);
            if (!"ZMQ".equals(portName)) {
                if (serialPort != null && serialPort.isOpened()) {
                    try {
                        serialPort.closePort();
                    } catch (SerialPortException ex) {
                        serialPortException.printStackTrace();
                    }
                }
            }
        }
    }

    private void showError(String text, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error alert");
        alert.setContentText(text);
        alert.setHeaderText(e.getMessage());

        VBox dialogPaneContent = new VBox();

        Label label = new Label("Stack Trace:");


        e.printStackTrace();
        String stackTrace = getStackTrace(e);
        TextArea textArea = new TextArea();
        textArea.setText(stackTrace);

        dialogPaneContent.getChildren().addAll(label, textArea);

        alert.getDialogPane().setContent(dialogPaneContent);

        alert.showAndWait();
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String s = sw.toString();
        return s;
    }

}

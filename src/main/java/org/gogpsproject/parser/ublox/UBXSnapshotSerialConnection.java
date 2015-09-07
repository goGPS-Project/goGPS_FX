package org.gogpsproject.parser.ublox;

import java.util.List;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

public class UBXSnapshotSerialConnection extends UBXSerialConnection {

  public UBXSnapshotSerialConnection(String portName, int speed) {
    super( portName, speed );
  }

  public void serialinit() throws Exception {
    CommPortIdentifier portIdentifier;

    portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
    if (portIdentifier.isCurrentlyOwned()) {
      System.out.println("Error: Port is currently in use");
    } else {
      serialPort = (SerialPort) portIdentifier.open("Serial", 2000);
      serialPort.setSerialPortParams(speed, SerialPort.DATABITS_8,
          SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

      inputStream = serialPort.getInputStream();
      outputStream = serialPort.getOutputStream();
    }
  }

  /* (non-Javadoc)
   * @see org.gogpsproject.StreamResource#init()
   */
  @Override
  public void init() throws Exception {
        serialinit();
        
        prod = new UBXSnapshotSerialReader(inputStream,outputStream,portName,outputDir);
        prod.setRate(this.setMeasurementRate);
        prod.enableAidEphMsg(this.setEphemerisRate);
        prod.enableAidHuiMsg(this.setIonosphereRate);
        prod.enableSysTimeLog(this.enableTimetag);
        prod.enableDebugMode(this.enableDebug);
        prod.enableNmeaMsg(this.enableNmeaList);
        prod.start();

        connected = true;
        System.out.println("Connection on " + portName + " established");
        //conn = true;
  }

}


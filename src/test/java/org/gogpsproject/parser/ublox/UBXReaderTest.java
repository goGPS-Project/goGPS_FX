package org.gogpsproject.parser.ublox;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Vector;

import org.gogpsproject.Constants;
import org.gogpsproject.ObservationSet;
import org.gogpsproject.Observations;
import org.gogpsproject.util.UnsignedOperation;
import org.junit.Test;
import static org.junit.Assert.*;

public class UBXReaderTest {

  public static class ByteBufferBackedInputStream extends InputStream {

    ByteBuffer buf;

    public ByteBufferBackedInputStream(ByteBuffer buf) {
        this.buf = buf;
    }

    public int read() throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }
        return buf.get() & 0xFF;
    }

    public int read(byte[] bytes, int off, int len)
            throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }

        len = Math.min(len, buf.remaining());
        buf.get(bytes, off, len);
        return len;
    }
 }
  
  public final int uBloxPrefix1 = 0xB5;
  public final  int uBloxPrefix2 = 0x62;

  private int CK_A;
  private int CK_B;
  private Vector<Integer> msg;

  public byte[] getByte() {
    byte[] bytes = new byte[msg.size()];
    for (int i = 0; i < msg.size(); i++) {
      bytes[i] = UnsignedOperation.unsignedIntToByte(((Integer)msg.elementAt(i)).intValue());
    }
    return bytes;
  }

  private void checkSum() {
    CK_A = 0;
    CK_B = 0;
    for (int i = 2; i < msg.size(); i++) {
      CK_A = CK_A + ((Integer) msg.elementAt(i)).intValue();
      CK_B = CK_B + CK_A;

    }
    CK_A = CK_A & 0xFF;
    CK_B = CK_B & 0xFF;
  }

  @Test
  public void UBXRateConfigurationTest() throws IOException, UBXException {
    int measRate = 1000;
    int navRate = 1;
    int timeRef = 10;
    byte[] measRateBytes = ByteBuffer.allocate(4).putInt(measRate).array();
    byte[] navRateBytes = ByteBuffer.allocate(4).putInt(navRate).array();
    byte[] timeRefBytes = ByteBuffer.allocate(4).putInt(timeRef).array();
    msg = new Vector<Integer>();
    msg.addElement(new Integer(uBloxPrefix1));
    msg.addElement(new Integer(uBloxPrefix2));
    msg.addElement(new Integer(0x06)); // CFG
    msg.addElement(new Integer(0x08)); // RATE
    msg.addElement(new Integer(6)); // length low
    msg.addElement(new Integer(0)); // length hi
    msg.addElement(new Integer(measRateBytes[3]));
    msg.addElement(new Integer(measRateBytes[2]));
    msg.addElement(new Integer(navRateBytes[3]));
    msg.addElement(new Integer(navRateBytes[2]));
    msg.addElement(new Integer(timeRefBytes[3]));
    msg.addElement(new Integer(timeRefBytes[2]));
    checkSum();
    msg.addElement(new Integer(CK_A));
    msg.addElement(new Integer(CK_B));
    
//    ByteArrayInputStream in = new ByteArrayInputStream(getByte());
//    DecodeRXMRAW decodegps = new DecodeRXMRAW(in);
//    Observations o = decodegps.decode(null);
//    assertNotNull(o);
  }

  @Test
  public void UBX_RXM_RAWTest() throws IOException, UBXException {
    long iTOW = 278368l; //ms
    int week = 1850;
    int numSV = 1; // # of sats in the message
    int pl_length = 8 + 24*numSV;
    int msg_length = 4 + 2 + pl_length + 2;

    msg = new Vector<Integer>();
    msg.addElement(new Integer(uBloxPrefix1));
    msg.addElement(new Integer(uBloxPrefix2));
    
    msg.addElement(new Integer(0x02)); // RXM
    msg.addElement(new Integer(0x10)); // RAW
    
    msg.addElement(new Integer(pl_length & 0xFF)); // length low
    msg.addElement(new Integer(pl_length >> 8)); // length hi
    
    // I4 iTOW 
    msg.addElement((int)(iTOW & 0xFF)); 
    msg.addElement((int)((iTOW >>  8)& 0xFF)); 
    msg.addElement((int)((iTOW >> 16)& 0xFF)); 
    msg.addElement((int)((iTOW >> 24)& 0xFF)); 
    
    // I2 week 
    msg.addElement((int)(week & 0xFF)); 
    msg.addElement((int)((week >>  8)& 0xFF)); 
    
    // U1 numSV 
    msg.addElement(numSV); 

    // U1 reserved 
    msg.addElement(0x00); 

    double cpMes= Double.NaN;
    double prMes = 68*Constants.SPEED_OF_LIGHT/1000;
    float  doMes = 3000;
    int svId = 18;
    int mesQI = 4;
    int cno = 32;
    int lli = 0;

    // SV for each 
    for( int sv=0; sv<numSV; sv++){
      // R8 cpMes 
      long cpMesl = Double.doubleToLongBits(cpMes);
      msg.addElement((int)(cpMesl & 0xFF)); 
      msg.addElement((int)((cpMesl >>  8)& 0xFF)); 
      msg.addElement((int)((cpMesl >> 16)& 0xFF)); 
      msg.addElement((int)((cpMesl >> 24)& 0xFF)); 
      msg.addElement((int)((cpMesl >> 32)& 0xFF)); 
      msg.addElement((int)((cpMesl >> 40)& 0xFF)); 
      msg.addElement((int)((cpMesl >> 48)& 0xFF)); 
      msg.addElement((int)((cpMesl >> 56)& 0xFF)); 
      
      // R8 prMes 
      long prMesl = Double.doubleToLongBits(prMes);
      msg.addElement((int)(prMesl & 0xFF)); 
      msg.addElement((int)((prMesl >>  8)& 0xFF)); 
      msg.addElement((int)((prMesl >> 16)& 0xFF)); 
      msg.addElement((int)((prMesl >> 24)& 0xFF)); 
      msg.addElement((int)((prMesl >> 32)& 0xFF)); 
      msg.addElement((int)((prMesl >> 40)& 0xFF)); 
      msg.addElement((int)((prMesl >> 48)& 0xFF)); 
      msg.addElement((int)((prMesl >> 56)& 0xFF)); 
          
      // R4 doMes 
      int doMesi = Float.floatToIntBits(doMes);
      msg.addElement((int)(doMesi & 0xFF)); 
      msg.addElement((int)((doMesi >>  8)& 0xFF)); 
      msg.addElement((int)((doMesi >> 16)& 0xFF)); 
      msg.addElement((int)((doMesi >> 24)& 0xFF)); 
      
      // U1 sv 
      msg.addElement(svId); 
      
      // I1 mesQI
      msg.addElement(mesQI); 
      
      // I1 cno
      msg.addElement(cno); 
      
      // U1 lli
      msg.addElement(lli); 
    }

    checkSum();
    msg.addElement(new Integer(CK_A));
    msg.addElement(new Integer(CK_B));
    
    ByteArrayInputStream in = new ByteArrayInputStream(getByte());
    in.read();
    in.read();
    in.read();
    in.read();
    DecodeRXMRAW decodegps = new DecodeRXMRAW(in);
    Observations o = decodegps.decode(null);
    assertNotNull(o);
    
    assertEquals( 1, o.getNumSat() );
    ObservationSet obs = o.getSatByIdx(0);
    assertEquals( cpMes, obs.getPhaseCycles(0), 0.01 );
    assertEquals( prMes, obs.getCodeC(0), 0.01 );
    assertEquals( doMes, obs.getDoppler(0), 0.01);
//    assertEquals( mesQI, obs.getQualityInd(0));
    assertEquals( cno, obs.getSignalStrength(0), 0.01 );
//    assertEquals( 0, obs.getLossLockInd(0));
    assertEquals( svId, obs.getSatID());
  }
  
}

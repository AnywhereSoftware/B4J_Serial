package anywheresoftware.b4j.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.collections.List;

@Version(1.10f)
@ShortName("Serial")
@DependsOn(values={"jssc"})
public class Serial {
	public static final int PURGE_RXABORT = 0x0002;
	public static final int PURGE_RXCLEAR = 0x0008;
	public static final int PURGE_TXABORT = 0x0001;
	public static final int PURGE_TXCLEAR = 0x0004;
	@Hide
	public SerialPort sp;
	@SuppressWarnings("unused")
	private String eventName;
	/**
	 * Initializes the object and sets the subs that will handle events (as of version 1.00 there are no events).
	 */
	public void Initialize( String EventName) {
		this.eventName = EventName.toLowerCase(BA.cul);
	}
	/**
	 * Returns a list with the ports names.
	 */
	public List ListPorts() {
		List l1 = new List();
		l1.Initialize();
		l1.getObject().addAll(Arrays.asList(SerialPortList.getPortNames()));
		return l1;
	}
	/**
	 * Sends a break signal.
	 */
	public boolean SendBreak(int Duration) throws SerialPortException {
		return sp.sendBreak(Duration);
	}
	/**
	 * Gets the state of the DSR line.
	 */
	public boolean getDSR() throws SerialPortException {
		return sp.isDSR();
	}
	/**
	 * Sets the state of the DTR line.
	 */
	public void setDTR(boolean b) throws SerialPortException {
		sp.setDTR(b);
	}
	/**
	 * Gets the state of the CTS line.
	 */
	public boolean getCTS() throws SerialPortException {
		return sp.isCTS();
	}
	/**
	 * Sets the state of the RTS line.
	 */
	public void setRTS(boolean b) throws SerialPortException {
		sp.setRTS(b);
	}
	/**
	 * Opens a port.
	 */
	public void Open(String PortName) throws SerialPortException {
		sp = new SerialPort(PortName);
		sp.openPort();

	}
	/**
	 * Purges the input and output buffers. PurgeFlags should be one of the PURGE constants
	 *or any combination (with Bit.Or).
	 */
	public void PurgePort(int PurgeFlags) throws SerialPortException {
		sp.purgePort(PurgeFlags);
	}
	/**
	 * Sets the connection parameters.
	 *You should only call this method after the port was opened.
	 */
	public void SetParams(int BaudRate, int DataBits, int StopBits, int Parity) throws SerialPortException {
		sp.setParams(BaudRate, DataBits, StopBits, Parity);
	}
	/**
	 * Closes the port. Nothing happens if the port is not open.
	 */
	public boolean Close() throws SerialPortException {
		if (sp != null && sp.isOpened())
			return sp.closePort();
		else
			return true;
	}
	/**
	 * Returns an InputStream that can be used with AsyncStreams.
	 */
	public InputStream GetInputStream() {
		return new InputStream() {

			@Override
			public int read() throws IOException {
				throw new RuntimeException("This method is not supported.");
			}
			@Override
			public int read(byte b[]) throws IOException {
				return read(b, 0, b.length);
			}
			@Override
			public int read(byte b[], int off, int len) throws IOException {
				try {
					while (sp.getInputBufferBytesCount() == 0) {
						Thread.sleep(10);
					}
					byte[] read = sp.readBytes(Math.min(sp.getInputBufferBytesCount(), len));
					System.arraycopy(read, 0, b, off, read.length);
					return read.length;
				} catch (InterruptedException ie) {
					throw new RuntimeException(ie);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
			@Override
			public void close() throws IOException {
				try {
					Close();
				} catch (SerialPortException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}
	/**
	 * Returns an OutputStream for working with AsyncStreams.
	 */
	public OutputStream GetOutputStream() {
		return new OutputStream() {

			@Override
			public void write(int b) throws IOException {
				throw new RuntimeException("This method is not supported.");
			}
			@Override
			public void write(byte b[]) throws IOException {
				write(b, 0, b.length);
			}
			@Override
			public void write(byte b[], int off, int len) throws IOException {
				try {
					byte[] dataToWrite;
					if (off == 0 && len == b.length)
						dataToWrite = b;
					else {
						dataToWrite = new byte[len];
						System.arraycopy(b, off, dataToWrite, 0, len);
					}
					if (!sp.writeBytes(b))
						throw new RuntimeException("Failed to write data. Data length = " + b.length);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
			@Override
			public void close() throws IOException {
				try {
					Close();
				} catch (SerialPortException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}



}

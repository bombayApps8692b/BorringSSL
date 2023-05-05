/**
 * 
 */
package android_serialport_api;

import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPortDevice {

	private FileDescriptor mFd;
	private InputStream mInputStream;
	private OutputStream mOutputStream;
	
	private String path;
	private int baudrate;
	int flags=0;

	public SerialPortDevice(String path, int baudrate, int flags) {
		mFd = new FileDescriptor();
		this.path=path;
		this.baudrate=baudrate;
		this.flags=flags;
	}

	/* (non-Javadoc)
	 * @see com.gps.device.IDevice#connect()
	 */
	public boolean connect()  {
		mFd = open(path, baudrate, 0);
		Log.i("xxx", "mFd==>"+mFd);
		if (mFd == null) {
			return false;
		}
		else
		{
			return true;
		}
	}	

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gps.device.IDevice#getInputStream()
	 */
	public InputStream getInputStream() {
		// TODO Auto-generated method stub
		return new FileInputStream(mFd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gps.device.IDevice#getOutputStream()
	 */
	public OutputStream getOutputStream() {
		// TODO Auto-generated method stub
		return new FileOutputStream(mFd);
	}

	// JNI
	private native static FileDescriptor open(String path, int baudrate, int flags);
	public native void close();

    static {
		System.loadLibrary("serial_port1");
	}	
}

package misc;

import android.content.Context;

import com.pax.mposapi.BaseSystemException;
import com.pax.mposapi.BaseSystemManager;
import com.pax.mposapi.CommonException;
import com.pax.mposapi.ProtoException;

import java.io.IOException;

public class TermSpecific {

	private static final int TERM_D800 = 0x91;
	private static final int TERM_D200 = 15;
	private static final int TERM_D210 = 16;
	private static final int TERM_D180 = 0xb4;
	private static final int TERM_D180_n = 0x23;
	
	private static TermSpecific ts;
	private static Context context;
	
	private byte[] termInfo;
	private BaseSystemManager base;
	
	private void init() throws IOException, ProtoException, BaseSystemException, CommonException {
		/*
		if (termInfo != null) {
			return;
		}
		*/
		if (base == null) {
			base = BaseSystemManager.getInstance(context);
		}
		
		termInfo = base.getTermInfo();
	}
	
	public static TermSpecific getInstance(Context ct) {
		if (ts == null) {
			ts = new TermSpecific();
			context = ct;
		}
		return ts;
	}
	
	public boolean isSupportProcessImage() throws IOException, ProtoException, BaseSystemException, CommonException {
		init();
		return ((termInfo[0] != TERM_D800) && (termInfo[0] != TERM_D180) && (termInfo[0] != TERM_D180_n));	//FIXME! what about d180?
	}
	
	public boolean hasPrinter() throws IOException, ProtoException, BaseSystemException, CommonException {
		init();
		return (termInfo[1] != 0);
	}

	public boolean hasPicc() throws IOException, ProtoException, BaseSystemException, CommonException {
		init();
		return (termInfo[12] != 0);
	}
	
	public boolean isScr12864() throws IOException, ProtoException, BaseSystemException, CommonException {
		init();
		return ( (termInfo[0] == (byte)TERM_D180) || (termInfo[0] == (byte)TERM_D180_n) );
	}
	
	public boolean isLandScape() {
		return false;
		/*
		 * FIXME!
		 * this will lead to communication during startup!
		try {
			init();
			return (termInfo[0] == TERM_D800);
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		*/
	}
}

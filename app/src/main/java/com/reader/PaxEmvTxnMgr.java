package com.reader;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.data.TRACE;
import com.mosambee.mpos.cpoc.R;
import com.activity.ActiveActivity;
import com.activity.Controller;
import com.connection.ConnectionManager.PostType;
import com.pax.mposapi.BaseSystemManager;
import com.pax.mposapi.ClssManager;
import com.pax.mposapi.CommonException;
import com.pax.mposapi.DataModel;
import com.pax.mposapi.EmvException;
import com.pax.mposapi.EmvManager;
import com.pax.mposapi.IccException;
import com.pax.mposapi.IccManager;
import com.pax.mposapi.MagException;
import com.pax.mposapi.MagManager;
import com.pax.mposapi.PedException;
import com.pax.mposapi.PedManager;
import com.pax.mposapi.PiccManager;
import com.pax.mposapi.ProtoException;
import com.pax.mposapi.UIException;
import com.pax.mposapi.UIManager;
import com.pax.mposapi.model.EMV_APPLIST;
import com.pax.mposapi.model.EMV_CANDLIST;
import com.pax.mposapi.model.EMV_CAPK;
import com.pax.mposapi.model.EMV_MCK_PARAM;
import com.pax.mposapi.model.EMV_PARAM;
import com.pax.mposapi.util.MyLog;
import com.pax.mposapi.util.Utils;
import com.ui.MTransactionDialog.FlipView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import misc.Misc;
import misc.StartTxn;
import misc.TermSpecific;
import misc.util;

public class PaxEmvTxnMgr {
	public PaxEmvTxnMgr(Controller controller, ActiveActivity activity, int amount) {
		this.transAmt = amount;
		this.activity = activity;
		this.controller = controller;
		base = BaseSystemManager.getInstance(activity);
		ui = UIManager.getInstance(activity);
		ped = PedManager.getInstance(activity);
		mag = MagManager.getInstance(activity);
		picc = PiccManager.getInstance(activity);
		emv = EmvManager.getInstance(activity);
		clss = ClssManager.getInstance(activity);
		icc = IccManager.getInstance(activity);
		txnMap = (HashMap<String, String>) controller.getTranscationMap();
		TRACE.i("PaxEmvTxnMgr----------------");
	}

	private static final String TAG = "PaxEmvTxnMgr";
	Activity activity;
	Controller controller;
	int transAmt;

	private final int MAG_CARD = 0;
	private final int EMV_CARD = 2;
	private int cardType;
	private String str[] = null;
	private int selectedArg = 0;
	private boolean needCashback = false;
	private String panString = "?";
	private boolean isTrackClear = true;
	String[] trackData;
	private String[] tracks = null;
	PedManager.PinDukptOutput pinBlockArray = null;

	private byte[] ksnPin = null;
	private byte[] pinBlock = null;

	private Thread swipeThread;
	private BaseSystemManager base;
	private UIManager ui;
	private PedManager ped;
	private MagManager mag;
	private IccManager icc;
	private EmvManager emv;
	private PiccManager picc;
	private ClssManager clss;
	
    private final String TRANSACTION_MODE_KEY = "transactionMode";
    private final String MODE_EMV_TRANSACTION = "1";
    private final String MODE_MSR_TRANSACTION = "2";
    private final String MODE_MSR_FALLBACK_TRANSACTION = "3";
    private final String TERMINAL_TRANSACTION_RESULT = "terminalTxnResult";
    private final String RESULT_MSR = "0";
    private final String RESULT_ONLINE_AUTH_REQUIRED = "1";
    private final String RESULT_TRANSACTOIN_APPROVED = "2";
    private final String RESULT_TRANSACTOIN_DECLINED = "3";
    HashMap<String, String> txnMap=null;
	StringBuilder pinBlockData = new StringBuilder(),
			ksnData = new StringBuilder();

	public void start() {
		MyLog.DEBUG_V = false;
		MyLog.DEBUG_D = false;
		MyLog.DEBUG_I = false;
		MyLog.DEBUG_W = false;
		MyLog.DEBUG_E = false;
		TRACE.i("start----------------");
		swipeThread = new Thread(swipeRunnable);
		running = true;
		swipeThread.start();
	}
	
	private String getPanIso9564_0(String pan) {
		int len = pan.length();

		if (len > 19 || len < 13) {
			return null;
		}

		String iso9564_0 = "0000" + pan.substring(len - 13, len - 1);

		return iso9564_0;
	}
	boolean isFallback = false;
	Runnable swipeRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Looper.prepare();
			if(!running){
				try {
					base.closeConnection();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}
			isFallback = false;
			txnMap=(HashMap<String, String>) controller.getTranscationMap();
			// fake terminal, returns directly
			if (running&&Misc.isDemoMode()) {
				controller.handleFlips(FlipView.DEVICE_POWER_ON, "");
				return;
			}
			
			controller.handleFlips(FlipView.CONNECTING);
			if (!base.ping()) {
				controller.handleFlips(FlipView.DEVICE_POWER_ON,"Connection Failed");
				return;
			}
			try {
				base.beep();
				boolean hasPicc = TermSpecific.getInstance(activity).hasPicc();
				ui.scrCls();
				mag.magOpen();
				mag.magReset();
				controller.handleFlips(FlipView.INSERT_CARD);
				ui.scrCls();
				ui.scrShowText("%P1400%F1" + activity.getString(R.string.swipe)+  "%P1410" + activity.getString(R.string.insert)+  "%P1420" + activity.getString(R.string.tap));
				String record = "";
				String maskedPan = new String();
				String serviceCode ="";
				cardType = -1;
				
				while (!Thread.interrupted()&&running) {
					Thread.sleep(500);
					if (mag.magSwiped()) {
						MagManager.TrackDataOutput track = mag.magRead(DataModel.EncryptionMode.SENSITIVE_CIPHER_DUKPTDES,(byte)3);
						String str1 = new String(track.track1, "iso-8859-1");
						String str2 = new String();
						String str3 = new String(track.track3, "iso-8859-1");
						byte[] str4 = track.ksn;
						String str5 = track.maskedPan;
						byte[] str6 = track.serviceCode;
						StringBuilder ksn = new StringBuilder();
						
						for (int i = 0; i < track.track2.length; i++) {
							str2 += String.format("%02X", track.track2[i]);
						}
						record += "Track1: " + str1 + "\n\n";
						record += "Track2: " + str2 + "\n\n";
						record += "Track3: " + str3 + "\n\n";
						record += "KSN: ";
						maskedPan = str5;
						for (int i = 0; i < str4.length; i++) {
							ksn.append(String.format("%02X", str4[i]));
						}
						record += ksn + "\n\n";
						record += "MASKEDPAN: " + str5 + "\n\n";
						record += "ServiceCode: ";
						
						serviceCode = new String(str6);
						record += "\n\n" + serviceCode;
						trackData = new String[5];
						trackData[0] = str1;
						trackData[1] = str2;
						trackData[2] = ksn.toString();
						cardType = MAG_CARD;
										TRACE.i("serviceCode::::"+serviceCode);

						  if((serviceCode.startsWith("2")||serviceCode.startsWith("6"))&&isFallback!=true){
							  controller.handleFlips(FlipView.INSERT_CARD);
							//  ui.scrShowText("%P1400%F0" +"Chip enabled card"+  "%P1410" + "Please insert card");
							  ui.scrCls();
							  ui.scrShowText("%P1400%F0" +"Chip enabled card"+"%P1410"+"%F0"+"Please insert card");
							  cardType=-1;
						  		}
						if(cardType!=-1)
						break;
					}
					if (emvDetect()) {
					int i=	emvtempStart();//fallback
					if(i==-6||i==-9){
						while(emvDetect())
						if(serviceCode.startsWith("2")||serviceCode.startsWith("6")){
							isFallback=true;
						}
					}else{
						cardType = EMV_CARD;
						break;
					}
					
					}
					
				}
				mag.magClose();
				if (cardType < 0) {
					return;
				}
				tracks = new String[3];
				if (cardType == EMV_CARD) {
					int emvResult=emvProc();
					 if (emvResult != EmvManager.EMV_AC_ARQC) {
						Log.e(TAG, "emv proc failed!");
						return;
					} 
				} else if (cardType == MAG_CARD) {
					trackData[3] = "";
					trackData[4] ="";
				//	if(serviceCode.endsWith("0")||serviceCode.endsWith("6")){
					String pinData ="5081591115300203";// getPanIso9564_0(maskedPan);
					Log.i(TAG, "pan is: " + maskedPan);
					controller.handleFlips(FlipView.PIN_ENTRY, false, "");
					ui.scrCls();
					ui.scrShowText("%P1400%F1" + "INR" + ":"+ Misc.cents2Yuan(transAmt) + "%P1410"+ activity.getString(R.string.waitingForInputPin)+ ":" + "%P1420");
					pinBlockArray = ped.pedGetPinDukpt((byte) 4, "4,5,6",null,PedManager.PED_PINBLOCK_ISO9564_0, 30000);
				//	pinBlock = ped.pedGetPinBlock((byte)3, "4,5,6","".getBytes(),PedManager.PED_PINBLOCK_ISO9564_0, 30000);
					pinBlock = pinBlockArray.pinBlockOut;
					ksnPin = pinBlockArray.ksnOut;
					for (int i = 0; i < pinBlock.length; i++) {
						pinBlockData.append(String.format("%02X", pinBlock[i]));
					}
					for (int i = 0; i < ksnPin.length; i++) {
						ksnData.append(String.format("%02X", ksnPin[i]));
					}
					trackData[3] = pinBlockData.toString();
					trackData[4] = ksnData.toString();
				//	}
					ui.scrCls();
					ui.scrShowText("%P1400%F1" + "INR" + ":"+ Misc.cents2Yuan(transAmt) + "%P1410"+ "Processing..");
					controller.handleFlips(FlipView.CONNECTING);
					Map<String, String> map=new HashMap<String, String>();
					map.put("5F20",controller.getHexForString(trackData[0]));
					map.put("DFAE02",trackData[1]);
					map.put("DFAE03",trackData[2]);
					map.put("DFAE04",trackData[3]);
					map.put("DFAE05",trackData[4]);
					if(!isFallback)
					map.put(TRANSACTION_MODE_KEY,MODE_MSR_TRANSACTION);
					else
						map.put(TRANSACTION_MODE_KEY,MODE_MSR_FALLBACK_TRANSACTION);
					map.put(TERMINAL_TRANSACTION_RESULT,RESULT_MSR);
					controller.setTranscationMap(map);
					isFallback=false;
					controller.handleTransaction();
					return;
				}
				if (cardType == MAG_CARD && tracks[1] == null) {
					Log.w(TAG, "no track2 read!");
					controller.handleFlips(FlipView.DECLINED,"Transaction Declined");
					showText("Transaction " + "%P1410" + "Declined");
					return;
				}
			//	loadIdleScreen();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				controller.handleFlips(FlipView.DECLINED);
				showText("Transaction " + "%P1410" + "Declined");
				e.printStackTrace();
				/*
				 * } catch (BaseSystemException e) { // TODO Auto-generated
				 * catch block e.printStackTrace(); } catch (IOException e) { //
				 * TODO Auto-generated catch block e.printStackTrace(); } catch
				 * (ProtoException e) { // TODO Auto-generated catch block
				 * e.printStackTrace(); } catch (CommonException e) { // TODO
				 * Auto-generated catch block e.printStackTrace(); } catch
				 * (UIException e) { // TODO Auto-generated catch block
				 * e.printStackTrace(); } catch (MagException e1) { // TODO
				 * Auto-generated catch block e1.printStackTrace(); } catch
				 * (PiccException e1) { // TODO Auto-generated catch block
				 * e1.printStackTrace(); } catch (EmvException e) { // TODO
				 * Auto-generated catch block
				 * 
				 * } catch (IccException e) { // TODO Auto-generated catch block
				 * e.printStackTrace(); } catch (InterruptedException e) { //
				 * TODO Auto-generated catch block e.printStackTrace(); } catch
				 * (PedException e) { // TODO Auto-generated catch block
				 * e.printStackTrace(); }
				 */

			}

		}
	};

	

	

	private boolean emvDetect() throws IccException, IOException,
			ProtoException, CommonException {
		// detect card
		TRACE.i("emvDetect----------------");
		return running&&icc.iccDetect(IccManager.ICC_SLOT_BIT_SLOT0);
	}

	public void wakeUp(int arg2) {
		selectedArg = arg2;
		synchronizer.wakeItUp();
	}

	public void showText(String text) {
		try {
			ui.scrCls();
			ui.scrShowText("%P1400%F1" + text);
		} catch (UIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CommonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	EMV_APPLIST app, app1, app2, app3, app4, app5,app6,app7,app8, app9,app10,app11,app12;;
private int emvtempStart() throws IOException, ProtoException, CommonException, UIException, EmvException, IccException {
	// TODO Auto-generated method stub
	TRACE.i("emvtempStart----------------");
	emv.setCallbackHandler(new EmvManager.EmvCallbackHandler() {
		@Override
		public int onWaitAppSel(int tryCnt, int appNum, EMV_APPLIST[] apps)
				throws IOException, ProtoException {
			Log.i(TAG, String.format("onWaitAppSel: tryCnt %d, appNum %d",
					tryCnt, appNum));
			try {
				ui.scrCls();
				ui.scrShowText("%P1400%F1 app selection");
			} catch (UIException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (CommonException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			str = new String[apps.length];
			for (int i = 0; i < appNum; i++) {
				str[i] = new String(apps[i].AppName, "iso-8859-1");

			}
			synchronizer.dontWakeup();
			try {
				synchronizer.waitForResult(300000);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
				return EmvManager.EMV_USER_CANCEL;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return selectedArg;
		}

		@Override
		public int onCandAppSel(int tryCnt, int appNum, EMV_CANDLIST[] apps)
				throws IOException, ProtoException {
			Log.i(TAG, String.format("onCandAppSel: tryCnt %d, appNum %d",
					tryCnt, appNum));
			TRACE.i("onCandAppSel----------------");
			try {
				ui.scrCls();
				ui.scrShowText("%F1App Selection");
			} catch (UIException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (CommonException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			str = new String[apps.length];
			for (int i = 0; i < appNum; i++) {
				str[i] = new String(apps[i].aucAppLabel, "iso-8859-1");
			}
			synchronizer.dontWakeup();
		//	controller.handleFlips(FlipView.AID_SELECTION, false, "", str);

			// sendMsgToMainHandler(PROMPT, String.valueOf(MSG_APP_LIST),
			// false, false);
			// handler.sendEmptyMessage(MSG_APP_LIST);
			try {
				synchronizer.waitForResult(300000);

			} catch (InterruptedException ie) {
				ie.printStackTrace();
				return EmvManager.EMV_USER_CANCEL;
			} catch (Exception e) {
				e.printStackTrace();
			}
			controller.handleFlips(FlipView.CONNECTING);
			Log.e("selected arg", String.valueOf(selectedArg));
			return selectedArg;
		}

		@Override
		public int onInputAmount(String[] amts) throws IOException,ProtoException {
			TRACE.i("onInputAmount----------------");

			needCashback = (amts[1] != null);
			Log.i(TAG, "onInputAmt need cashback: " + needCashback);
			int amt = transAmt;
			Log.i(TAG, "amt is : " + amt);
			amts[0] = String.format("%d", amt);

			if (needCashback) {
				int cb = 0; // FIXME!
				Log.i(TAG, "cashback is : " + cb);
				amts[1] = String.format("%d", cb);
			}

			return EmvManager.EMV_OK;
		}

		@Override
		public int onGetHolderPwd(int pinFlag, int tryFlag, int remainCnt,
				int pinStatus) throws IOException, ProtoException,
				CommonException {

			TRACE.i("onGetHolderPwd----------------");
		String	pinInfoString = String.format("pinflg:%d, tryFlg:%d/%d, st:%d",
					pinFlag, tryFlag, remainCnt, pinStatus);
			
			Log.i(TAG,String.format("onGetHolderPwd: pinFlag %d, tryFlag %d, remainCnt %d, pinStatus %d",pinFlag, tryFlag, remainCnt, pinStatus));
			if (pinFlag == EmvManager.EMV_PIN_FLAG_NO_PIN_REQUIRED) {
				Log.i(TAG, "no pin required");
				return EmvManager.EMV_OK;
			} else if (pinFlag == EmvManager.EMV_PIN_FLAG_ONLINE) {
				Log.i(TAG, "online pin");
				// controller.handleFlips(FlipView.PIN_ENTRY,
				// "Enter PIN :");
				// pinEntry();
				controller.handleFlips(FlipView.PIN_ENTRY, false, "");
				panString =getTag(0x5A, "PAN");
				PedManager.PinDukptOutput pinBlockArray = null;
				try {
					ui.scrCls();
					ui.scrShowText("%P1400%F1"+ "INR"+ ":"+ Misc.cents2Yuan(transAmt)+ "%P1410"+ activity.getString(R.string.waitingForInputPin)+ ":" + "%P1420");
					pinBlockArray = ped.pedGetPinDukpt((byte) 4, "4,5,6",panString.replace("F", "").getBytes(),PedManager.PED_PINBLOCK_ISO9564_0, 30000);
					pinBlockData = new StringBuilder();
					ksnData = new StringBuilder();
					pinBlock = pinBlockArray.pinBlockOut;
					ksnPin = pinBlockArray.ksnOut;
					for (int i = 0; i < pinBlock.length; i++) {
						pinBlockData.append(String.format("%02X",pinBlock[i]));
					}
					for (int i = 0; i < ksnPin.length; i++) {
						ksnData.append(String.format("%02X", ksnPin[i]));
					}
					ui.scrCls();
					ui.scrShowText("%P1400%F1" + "INR" + ":"+ Misc.cents2Yuan(transAmt) + "%P1410"+ "Processing..");
					controller.handleFlips(FlipView.CONNECTING);
				} catch (PedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return EmvManager.EMV_OK;
			} else if (pinFlag == EmvManager.EMV_PIN_FLAG_OFFLINE) {
				controller.handleFlips(FlipView.PIN_ENTRY, false, "");
				Log.i(TAG, "offline pin");
				panString =getTag(0x5A, "PAN");

				// cfg.receiveTimeout = offlinePinInputTimeout + 1000;
				// //increase receive timeout, so we c

				try {
					ui.scrCls();
					// ui.scrShowText("ENTER PIN: \n ");
					ui.scrShowText("%P1400%F1"
							+ "INR"+ ":"
							+ Misc.cents2Yuan(transAmt)
							+ "%P1410"
							+ activity
									.getString(R.string.waitingForInputPin)
							+ ":" + "%P1420");
				} catch (UIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (pinStatus == EmvManager.EMV_OFFLINE_PIN_STATUS_PED_TIMEOUT) {
					return EmvManager.EMV_TIME_OUT;
				} else if (pinStatus == EmvManager.EMV_OFFLINE_PIN_STATUS_PED_FAIL) {
					return EmvManager.EMV_USER_CANCEL;
				}
				return EmvManager.EMV_OK;
			}
			return EmvManager.EMV_USER_CANCEL;
		}

		@Override
		public int onReferProc() throws IOException, ProtoException {
			Log.i(TAG, "onReferProc");
			TRACE.i("onReferProc----------------");
			return EmvManager.EMV_REFER_APPROVE;
		}

		@Override
		public int onOnlineProc(byte[] respCode, byte[] authCode,
				byte[] authData, byte[] script) throws IOException,
				ProtoException {
			Log.i(TAG, "onOnlineProc");
			TRACE.i("onOnlineProc----------------");

			return EmvManager.EMV_ONLINE_DENIAL;
		}

		@Override
		public void onAdviceProc() throws IOException, ProtoException {
			Log.i(TAG, "onAdviceProc");
			TRACE.i("onAdviceProc----------------");
		}

		@Override
		public void onVerifyPinOk() throws IOException, ProtoException {
			Log.i(TAG, "onVerifyPinOk");
			// cfg.load(); //reload receive timeout
			TRACE.i("onVerifyPinOk----------------");
			try {
				ui.scrCls();
				ui.scrShowText("%P1400%F1" + "PIN OK");
				controller.handleFlips(FlipView.CONNECTING);
			} catch (UIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CommonException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public int onUnknownTLVData(short tag, int len, byte[] value)
				throws IOException, ProtoException {
			Log.i(TAG, String.format("onUnknownTLVData: tag %d, len %d",
					tag, len));
			TRACE.i("onUnknownTLVData----------------");

			return EmvManager.EMV_UNKNOWN_TAG_VALUE_IGNORED;
		}

		@Override
		public int onCertVerify() throws IOException, ProtoException {
			Log.i(TAG, "onCertVerify");
			TRACE.i("onCertVerify----------------");

			// TODO: cert ui
			return EmvManager.EMV_CERT_VERIFY_OK;
		}

		@Override
		public int onSetParam() throws IOException, ProtoException {
			Log.i(TAG, "onSetParam");
			TRACE.i("onSetParam----------------");
			return EmvManager.EMV_OK;
		}
	});
	emv.initTLVData();
	//String tc	=getTag(0x9f41, "transactionCounter");
	while (!icc.iccDetect(IccManager.ICC_SLOT_BIT_SLOT0)) {
		ui.scrCls();
		ui.scrShowText("%P1664%F2"
				+ activity.getString(R.string.waitingForCard));// &#xfffd;&#x234;&#xfffd;&#x5fe8;
		SystemClock.sleep(1000);
	}
	ui.scrCls();
	ui.scrShowText("%P1400%F1" + "INR" + ":" + Misc.cents2Yuan(transAmt)
			+ "%P1410" + "Processing..");
	controller.handleFlips(FlipView.CONNECTING);
	app = new EMV_APPLIST();
	byte[] appName = new String("VISA-ELECTRON").getBytes();
	System.arraycopy(appName, 0, app.AppName, 0, appName.length);
	byte[] aid = new byte[] { (byte) 0xA0, 0x00, 0x00, 0x00, 0x03, 0x20,
			0x10 }; // visa rid
	System.arraycopy(aid, 0, app.AID, 0, aid.length);
	app.AidLen = (byte) 0x07;
	app.FloorLimit = 000000;
	app.Threshold = 50000;
	app.SelFlag = 0;
	byte[] actDenial = new byte[] { 0x00, 0x10, 0x00, 0x00, 0x00 };
	System.arraycopy(actDenial, 0, app.TACDenial, 0, actDenial.length);
	byte[] actOnline = new byte[] { (byte) 0xD8, 0x40, 0x04, (byte) 0xF8,
			0x00 };
	System.arraycopy(actOnline, 0, app.TACOnline, 0, actOnline.length);
	byte[] actDefault = new byte[] { (byte) 0xD8, 0x40, 0x00, (byte) 0xA8,
			0x00 };
	System.arraycopy(actDefault, 0, app.TACDefault, 0, actDefault.length);
	byte[] aquirerId = new byte[] { 0x00, 0x00, 0x00, 0x12, 0x34, 0x56 };
	System.arraycopy(aquirerId, 0, app.AcquierId, 0, aquirerId.length);
	byte[] dDol = new byte[] { 0x03, (byte) 0x9F, 0x37, 0x04 };
	System.arraycopy(dDol, 0, app.dDOL, 0, dDol.length);
	byte[] tDol = new byte[] { (byte) 0x0F, (byte) 0x9F, 0x02, 0x06,
			(byte) 0x5F, (byte) 0x2A, 0x02, (byte) 0x9A, 0x03, (byte) 0x9C,
			0x01, (byte) 0x95, 0x05, (byte) 0x9F, 0x37, 0x04 };
	System.arraycopy(tDol, 0, app.dDOL, 0, tDol.length);
	byte[] version = new byte[] { 0x00, (byte) 0x8c };
	System.arraycopy(version, 0, app.Version, 0, version.length);

	app1 = new EMV_APPLIST();
	byte[] appName1 = new String("VISA-VSDC").getBytes();
	System.arraycopy(appName1, 0, app1.AppName, 0, appName1.length);
	byte[] aid1 = new byte[] { (byte) 0xA0, 0x00, 0x00, 0x00, 0x03, 0x10,
			0x10 }; // visa
	System.arraycopy(aid1, 0, app1.AID, 0, aid1.length);
	app1.AidLen = (byte) 0x07;
	app1.FloorLimit = 000000;
	app1.Threshold = 50000;
	app1.SelFlag = 0;
	byte[] actDenial1 = new byte[] { 0x00, 0x10, 0x00, 0x00, 0x00 };
	System.arraycopy(actDenial1, 0, app1.TACDenial, 0, actDenial1.length);
	byte[] actOnline1 = new byte[] { (byte) 0xD8, 0x40, 0x04, (byte) 0xF8,
			0x00 };
	System.arraycopy(actOnline1, 0, app1.TACOnline, 0, actOnline1.length);
	byte[] actDefault1 = new byte[] { (byte) 0xD8, 0x40, 0x00, (byte) 0xA8,
			0x00 };
	System.arraycopy(actDefault1, 0, app1.TACDefault, 0, actDefault1.length);
	byte[] aquirerId1 = new byte[] { (byte) 0x00, 0x00, 0x00, (byte) 0x21,
			0x34, 0x56 };
	System.arraycopy(aquirerId1, 0, app1.AcquierId, 0, aquirerId1.length);
	byte[] dDol1 = new byte[] { (byte) 0x03, (byte) 0x9F, 0x37, (byte) 0x04 };
	System.arraycopy(dDol1, 0, app1.dDOL, 0, dDol1.length);
	byte[] tDol1 = new byte[] { (byte) 0x0F, (byte) 0x9F, (byte) 0x02,
			(byte) 0x06, (byte) 0x5F, (byte) 0x2A, (byte) 0x02,
			(byte) 0x9A, (byte) 0x03, (byte) 0x9C, (byte) 0x01,
			(byte) 0x95, (byte) 0x05, (byte) 0x9F, (byte) 0x37, (byte) 0x04 };
	System.arraycopy(tDol1, 0, app1.dDOL, 0, tDol1.length);
	version = new byte[] { 0x00, (byte) 0x8c };
	System.arraycopy(version, 0, app1.Version, 0, version.length);

	app2 = new EMV_APPLIST();
	byte[] appName2 = new String("flash").getBytes();
	System.arraycopy(appName2, 0, app2.AppName, 0, appName2.length);
	byte[] aid2 = new byte[] { (byte) 0xA0, 0x00, 0x00, 0x03, 0x33, 0x00,
			0x00 }; // pboc rid
	System.arraycopy(aid2, 0, app2.AID, 0, aid2.length);
	app2.AidLen = (byte) 0x07;
	app2.FloorLimit = 000000;
	app2.Threshold = 50000;
	app2.SelFlag = 0;
	byte[] actDenial2 = new byte[] { 0x00, 0x10, 0x00, 0x00, 0x00 };
	System.arraycopy(actDenial2, 0, app2.TACDenial, 0, actDenial2.length);
	byte[] actOnline2 = new byte[] { (byte) 0xD8, 0x40, 0x04, (byte) 0xF8,
			0x00 };
	System.arraycopy(actOnline2, 0, app2.TACOnline, 0, actOnline2.length);
	byte[] actDefault2 = new byte[] { (byte) 0xD8, 0x40, 0x00, (byte) 0xA8,
			0x00 };
	System.arraycopy(actDefault2, 0, app2.TACDefault, 0, actDefault2.length);
	byte[] aquirerId2 = new byte[] { (byte) 0x01, 0x02, 0x03, (byte) 0x04,
			0x05, 0x06 };
	System.arraycopy(aquirerId2, 0, app2.AcquierId, 0, aquirerId2.length);
	byte[] dDol2 = new byte[] { (byte) 0x03, (byte) 0x9F, 0x37, (byte) 0x04 };
	System.arraycopy(dDol2, 0, app2.dDOL, 0, dDol2.length);
	byte[] tDol2 = new byte[] { (byte) 0x0F, (byte) 0x9F, (byte) 0x02,
			(byte) 0x06, (byte) 0x5F, (byte) 0x2A, (byte) 0x02,
			(byte) 0x9A, (byte) 0x03, (byte) 0x9C, (byte) 0x01,
			(byte) 0x95, (byte) 0x05, (byte) 0x9F, (byte) 0x37, (byte) 0x04 };
	System.arraycopy(tDol2, 0, app2.dDOL, 0, tDol2.length);
	version = new byte[] { 0x00, 0x02 };
	System.arraycopy(version, 0, app2.Version, 0, version.length);
	app3 = new EMV_APPLIST();
	byte[] appName3 = new String("pboc").getBytes();
	System.arraycopy(appName3, 0, app3.AppName, 0, appName3.length);
	byte[] aid3 = new byte[] { (byte) 0xA0, 0x00, 0x00, 0x00, 0x04 }; // pboc
																		// rid
	System.arraycopy(aid3, 0, app3.AID, 0, aid3.length);
	app3.AidLen = (byte) 0x05;
	app3.FloorLimit = 000000;
	app3.Threshold = 50000;
	app3.SelFlag = 0;
	byte[] actDenial3 = new byte[] { 0x04, 0x00, 0x00, 0x00, 0x00 };
	System.arraycopy(actDenial3, 0, app3.TACDenial, 0, actDenial3.length);
	byte[] actOnline3 = new byte[] { (byte) 0xF8, 0x50, (byte) 0xAC,
			(byte) 0xF8, 0x00 };
	System.arraycopy(actOnline3, 0, app3.TACOnline, 0, actOnline3.length);
	byte[] actDefault3 = new byte[] { (byte) 0xFC, 0x50, (byte) 0xAC,
			(byte) 0xA0, 0x00 };
	System.arraycopy(actDefault3, 0, app3.TACDefault, 0, actDefault3.length);
	byte[] aquirerId3 = new byte[] { (byte) 0x00, 0x00, 0x00, (byte) 0x12,
			0x34, 0x56 };
	System.arraycopy(aquirerId3, 0, app3.AcquierId, 0, aquirerId3.length);
	byte[] dDol3 = new byte[] { (byte) 0x03, (byte) 0x9F, 0x37, (byte) 0x04 };
	System.arraycopy(dDol3, 0, app3.dDOL, 0, dDol3.length);
	byte[] tDol3 = new byte[] { (byte) 0x0F, (byte) 0x9F, (byte) 0x02,
			(byte) 0x06, (byte) 0x5F, (byte) 0x2A, (byte) 0x02,
			(byte) 0x9A, (byte) 0x03, (byte) 0x9C, (byte) 0x01,
			(byte) 0x95, (byte) 0x05, (byte) 0x9F, (byte) 0x37, (byte) 0x04 };
	System.arraycopy(tDol3, 0, app3.dDOL, 0, tDol3.length);
	version = new byte[] { 0x00, 0x02 };
	System.arraycopy(version, 0, app3.Version, 0, version.length);

	app4 = new EMV_APPLIST();
	byte[] appName4 = new String("MasterCard").getBytes();
	System.arraycopy(appName4, 0, app4.AppName, 0, appName4.length);
	byte[] aid4 = new byte[] { (byte) 0xA0, 0x00, 0x00, 0x00, 0x04, 0x10,
			0x10 }; // pboc
	// rid
	System.arraycopy(aid4, 0, app4.AID, 0, aid4.length);
	app4.AidLen = (byte) 0x05;
	app4.FloorLimit = 000000;
	app4.Threshold = 50000;
	app4.SelFlag = 0;
	app4.VelocityCheck = 1;

	byte[] actDenial4 = new byte[] { 0x04, 0x00, 0x00, 0x00, 0x00 };
	System.arraycopy(actDenial4, 0, app4.TACDenial, 0, actDenial4.length);
	byte[] actOnline4 = new byte[] { (byte) 0xF8, 0x50, (byte) 0xBC,
			(byte) 0xF8, 0x00 };
	System.arraycopy(actOnline4, 0, app4.TACOnline, 0, actOnline4.length);
	byte[] actDefault4 = new byte[] { (byte) 0xFC, 0x50, (byte) 0xBC,
			(byte) 0xA0, 0x00 };
	System.arraycopy(actDefault4, 0, app4.TACDefault, 0, actDefault4.length);
	byte[] aquirerId4 = new byte[] { (byte) 0x00, 0x00, 0x00, (byte) 0x12,
			0x34, 0x56 };
	System.arraycopy(aquirerId4, 0, app4.AcquierId, 0, aquirerId4.length);
	byte[] dDol4 = new byte[] { (byte) 0x03, (byte) 0x9F, 0x37, (byte) 0x04 };
	System.arraycopy(dDol4, 0, app4.dDOL, 0, dDol4.length);
	byte[] tDol4 = new byte[] { (byte) 0x0F, (byte) 0x9F, (byte) 0x02,
			(byte) 0x06, (byte) 0x5F, (byte) 0x2A, (byte) 0x02,
			(byte) 0x9A, (byte) 0x03, (byte) 0x9C, (byte) 0x01,
			(byte) 0x95, (byte) 0x05, (byte) 0x9F, (byte) 0x37, (byte) 0x04 };
	System.arraycopy(tDol4, 0, app4.dDOL, 0, tDol4.length);
	byte[] version4 = new byte[] { 0x00, 0x02 };
	System.arraycopy(version4, 0, app4.Version, 0, version4.length);

	app5 = new EMV_APPLIST();
	byte[] appName5 = new String("Maestro").getBytes();
	System.arraycopy(appName5, 0, app5.AppName, 0, appName5.length);
	byte[] aid5 = new byte[] { (byte) 0xA0, 0x00, 0x00, 0x00, 0x05, 0x00,
			0x01 }; // pboc
	System.arraycopy(aid5, 0, app5.AID, 0, aid5.length);
	app5.AidLen = (byte) 0x05;
	app5.FloorLimit = 000000;
	app5.Threshold = 50000;
	app5.SelFlag = 0;
	app5.VelocityCheck = 1;
	byte[] actDenial5 = new byte[] { 0x04, 0x00, 0x00, 0x00, 0x00 };
	System.arraycopy(actDenial5, 0, app5.TACDenial, 0, actDenial5.length);
	byte[] actOnline5 = new byte[] { (byte) 0xF8, 0x50, (byte) 0xBC,
			(byte) 0xF8, 0x00 };
	System.arraycopy(actOnline5, 0, app5.TACOnline, 0, actOnline5.length);
	byte[] actDefault5 = new byte[] { (byte) 0xFC, 0x50, (byte) 0xBC,
			(byte) 0xA0, 0x00 };
	System.arraycopy(actDefault5, 0, app5.TACDefault, 0, actDefault5.length);
	byte[] aquirerId5 = new byte[] { (byte) 0x00, 0x00, 0x00, (byte) 0x12,
			0x34, 0x56 };
	System.arraycopy(aquirerId5, 0, app5.AcquierId, 0, aquirerId5.length);
	byte[] dDol5 = new byte[] { (byte) 0x03, (byte) 0x9F, 0x37, (byte) 0x04 };
	System.arraycopy(dDol5, 0, app.dDOL, 0, dDol5.length);
	byte[] tDol5 = new byte[] { (byte) 0x0F, (byte) 0x9F, (byte) 0x02,
			(byte) 0x06, (byte) 0x5F, (byte) 0x2A, (byte) 0x02,
			(byte) 0x9A, (byte) 0x03, (byte) 0x9C, (byte) 0x01,
			(byte) 0x95, (byte) 0x05, (byte) 0x9F, (byte) 0x37, (byte) 0x04 };
	System.arraycopy(tDol5, 0, app5.dDOL, 0, tDol5.length);
	byte[] version1 = new byte[] { 0x00, 0x02 };
	System.arraycopy(version1, 0, app5.Version, 0, version1.length);

	/*AID=A0000000049999*
	 * A000000004FFFF
			AID=A0000000043060
			A000000004FF01**/
	app6 = new EMV_APPLIST();
	byte[] appName6 = new String("Maestro").getBytes();
	System.arraycopy(appName6, 0, app6.AppName, 0, appName6.length);
	byte[] aid6 = new byte[] { (byte) 0xA0, 0x00, 0x00, 0x00, 0x04, 0x30,
			0x60 }; // pboc
	System.arraycopy(aid6, 0, app6.AID, 0, aid6.length);
	app6.AidLen = (byte) 0x05;
	app6.FloorLimit = 000000;
	app6.Threshold = 50000;
	app6.SelFlag = 0;
	app6.VelocityCheck = 1;
	byte[] actDenial6 = new byte[] { 0x04, 0x00, 0x00, 0x00, 0x00 };
	System.arraycopy(actDenial6, 0, app6.TACDenial, 0, actDenial6.length);
	byte[] actOnline6 = new byte[] { (byte) 0xF8, 0x50, (byte) 0xBC,
			(byte) 0xF8, 0x00 };
	System.arraycopy(actOnline6, 0, app6.TACOnline, 0, actOnline6.length);
	byte[] actDefault6 = new byte[] { (byte) 0xFC, 0x50, (byte) 0xBC,
			(byte) 0xA0, 0x00 };
	System.arraycopy(actDefault6, 0, app6.TACDefault, 0, actDefault6.length);
	byte[] aquirerId6 = new byte[] { (byte) 0x00, 0x00, 0x00, (byte) 0x12,
			0x34, 0x56 };
	System.arraycopy(aquirerId6, 0, app6.AcquierId, 0, aquirerId6.length);
	byte[] dDol6 = new byte[] { (byte) 0x03, (byte) 0x9F, 0x37, (byte) 0x04 };
	System.arraycopy(dDol6, 0, app6.dDOL, 0, dDol6.length);
	byte[] tDol6 = new byte[] { (byte) 0x0F, (byte) 0x9F, (byte) 0x02,
			(byte) 0x06, (byte) 0x5F, (byte) 0x2A, (byte) 0x02,
			(byte) 0x9A, (byte) 0x03, (byte) 0x9C, (byte) 0x01,
			(byte) 0x95, (byte) 0x05, (byte) 0x9F, (byte) 0x37, (byte) 0x04 };
	System.arraycopy(tDol6, 0, app6.dDOL, 0, tDol6.length);
	byte[] version6 = new byte[] { 0x00, 0x02 };
	System.arraycopy(version6, 0, app6.Version, 0, version6.length);	
	
	//A0 00 00 00 04 99 99
	app7 = new EMV_APPLIST();
	byte[] appName7 = new String("Maestro").getBytes();
	System.arraycopy(appName7, 0, app7.AppName, 0, appName7.length);
	byte[] aid7 = new byte[] { (byte) 0xA0, 0x00, 0x00, 0x00, 0x04, (byte) 0x99,
			(byte) 0x99 }; // pboc
	System.arraycopy(aid7, 0, app7.AID, 0, aid7.length);
	app7.AidLen = (byte) 0x05;
	app7.FloorLimit = 000000;
	app7.Threshold = 50000;
	app7.SelFlag = 0;
	app7.VelocityCheck = 1;
	byte[] actDenial7 = new byte[] { 0x04, 0x00, 0x00, 0x00, 0x00 };
	System.arraycopy(actDenial7, 0, app7.TACDenial, 0, actDenial7.length);
	byte[] actOnline7 = new byte[] { (byte) 0xF8, 0x50, (byte) 0xBC,
			(byte) 0xF8, 0x00 };
	System.arraycopy(actOnline7, 0, app7.TACOnline, 0, actOnline7.length);
	byte[] actDefault7 = new byte[] { (byte) 0xFC, 0x50, (byte) 0xBC,
			(byte) 0xA0, 0x00 };
	System.arraycopy(actDefault7, 0, app7.TACDefault, 0, actDefault7.length);
	byte[] aquirerId7 = new byte[] { (byte) 0x00, 0x00, 0x00, (byte) 0x12,
			0x34, 0x56 };
	System.arraycopy(aquirerId7, 0, app7.AcquierId, 0, aquirerId7.length);
	byte[] dDol7 = new byte[] { (byte) 0x03, (byte) 0x9F, 0x37, (byte) 0x04 };
	System.arraycopy(dDol7, 0, app7.dDOL, 0, dDol7.length);
	byte[] tDol7 = new byte[] { (byte) 0x0F, (byte) 0x9F, (byte) 0x02,
			(byte) 0x06, (byte) 0x5F, (byte) 0x2A, (byte) 0x02,
			(byte) 0x9A, (byte) 0x03, (byte) 0x9C, (byte) 0x01,
			(byte) 0x95, (byte) 0x05, (byte) 0x9F, (byte) 0x37, (byte) 0x04 };
	System.arraycopy(tDol7, 0, app7.dDOL, 0, tDol7.length);
	byte[] version7 = new byte[] { 0x00, 0x02 };
	System.arraycopy(version7, 0, app7.Version, 0, version7.length);	
	
	
	app8 = new EMV_APPLIST();
	byte[] appName8 = new String("Maestro").getBytes();
	System.arraycopy(appName8, 0, app8.AppName, 0, appName8.length);
	byte[] aid8 = new byte[] { (byte) 0xA0, 0x00, 0x00, 0x00, 0x04, (byte) 0x99,
			(byte) 0x99 }; // pboc
	
	System.arraycopy(aid8, 0, app8.AID, 0, aid8.length);
	app8.AidLen = (byte) 0x05;
	app8.FloorLimit = 000000;
	app8.Threshold = 50000;
	app8.SelFlag = 0;
	app8.VelocityCheck = 1;
	byte[] actDenial8 = new byte[] { 0x04, 0x00, 0x00, 0x00, 0x00 };
	System.arraycopy(actDenial8, 0, app8.TACDenial, 0, actDenial8.length);
	byte[] actOnline8 = new byte[] { (byte) 0xF8, 0x50, (byte) 0xBC,
			(byte) 0xF8, 0x00 };
	System.arraycopy(actOnline8, 0, app8.TACOnline, 0, actOnline8.length);
	byte[] actDefault8 = new byte[] { (byte) 0xFC, 0x50, (byte) 0xBC,
			(byte) 0xA0, 0x00 };
	System.arraycopy(actDefault8, 0, app8.TACDefault, 0, actDefault8.length);
	byte[] aquirerId8 = new byte[] { (byte) 0x00, 0x00, 0x00, (byte) 0x12,
			0x34, 0x56 };
	System.arraycopy(aquirerId8, 0, app8.AcquierId, 0, aquirerId8.length);
	byte[] dDol8 = new byte[] { (byte) 0x03, (byte) 0x9F, 0x37, (byte) 0x04 };
	System.arraycopy(dDol8, 0, app8.dDOL, 0, dDol8.length);
	byte[] tDol8 = new byte[] { (byte) 0x0F, (byte) 0x9F, (byte) 0x02,
			(byte) 0x06, (byte) 0x5F, (byte) 0x2A, (byte) 0x02,
			(byte) 0x9A, (byte) 0x03, (byte) 0x9C, (byte) 0x01,
			(byte) 0x95, (byte) 0x05, (byte) 0x9F, (byte) 0x37, (byte) 0x04 };
	System.arraycopy(tDol8, 0, app8.dDOL, 0, tDol8.length);
	byte[] version8 = new byte[] { 0x00, 0x02 };
	System.arraycopy(version8, 0, app8.Version, 0, version8.length);	
	
	
	
	
	app9 = new EMV_APPLIST();
	byte[] appName9 = new String("Diners").getBytes();
	System.arraycopy(appName9, 0, app9.AppName, 0, appName9.length);
	//A0 00 00 01 52 30 10
	byte[] aid9 = new byte[] { (byte) 0xA0, 0x00, 0x00, 0x01, 0x52, 0x30,
			0x10 }; // pboc
	
	System.arraycopy(aid9, 0, app9.AID, 0, aid9.length);
	app9.AidLen = (byte) 0x05;
	app9.FloorLimit = 000000;
	app9.Threshold = 50000;
	app9.SelFlag = 0;
	app9.VelocityCheck = 1;
	byte[] actDenial9 = new byte[] { 0x04, 0x00, 0x00, 0x00, 0x00 };
	System.arraycopy(actDenial9, 0, app9.TACDenial, 0, actDenial9.length);
	byte[] actOnline9 = new byte[] { (byte) 0xF8, 0x50, (byte) 0xBC,
			(byte) 0xF8, 0x00 };
	System.arraycopy(actOnline9, 0, app9.TACOnline, 0, actOnline9.length);
	byte[] actDefault9 = new byte[] { (byte) 0xFC, 0x50, (byte) 0xBC,
			(byte) 0xA0, 0x00 };
	System.arraycopy(actDefault9, 0, app9.TACDefault, 0, actDefault9.length);
	byte[] aquirerId9 = new byte[] { (byte) 0x00, 0x00, 0x00, (byte) 0x12,
			0x34, 0x56 };
	System.arraycopy(aquirerId9, 0, app9.AcquierId, 0, aquirerId9.length);
	byte[] dDol9 = new byte[] { (byte) 0x03, (byte) 0x9F, 0x37, (byte) 0x04 };
	System.arraycopy(dDol9, 0, app9.dDOL, 0, dDol9.length);
	byte[] tDol9 = new byte[] { (byte) 0x0F, (byte) 0x9F, (byte) 0x02,
			(byte) 0x06, (byte) 0x5F, (byte) 0x2A, (byte) 0x02,
			(byte) 0x9A, (byte) 0x03, (byte) 0x9C, (byte) 0x01,
			(byte) 0x95, (byte) 0x05, (byte) 0x9F, (byte) 0x37, (byte) 0x04 };
	System.arraycopy(tDol9, 0, app9.dDOL, 0, tDol9.length);
	byte[] version9 = new byte[] { 0x00, 0x01 };
	System.arraycopy(version9, 0, app9.Version, 0, version9.length);	
	
	
	
	
	app10 = new EMV_APPLIST();
	byte[] appName10 = new String("Rupay").getBytes();
	System.arraycopy(appName10, 0, app10.AppName, 0, appName10.length);
	//A00000 05 24 10 10
	byte[] aid10 = new byte[] { (byte) 0xA0, 0x00, 0x00, 0x05, 0x24, 0x10,
			 0x10 }; // pboc
	
	System.arraycopy(aid10, 0, app10.AID, 0, aid10.length);
	app10.AidLen = (byte) 0x05;
	app10.FloorLimit = 000000;
	app10.Threshold = 50000;
	app10.SelFlag = 0;
	app10.VelocityCheck = 1;
	byte[] actDenial10 = new byte[] { 0x04, 0x00, 0x00, 0x00, 0x00 };
	System.arraycopy(actDenial10, 0, app10.TACDenial, 0, actDenial10.length);
	byte[] actOnline10 = new byte[] { (byte) 0xF8, 0x50, (byte) 0xBC,
			(byte) 0xF8, 0x00 };
	System.arraycopy(actOnline10, 0, app10.TACOnline, 0, actOnline10.length);
	byte[] actDefault10 = new byte[] { (byte) 0xFC, 0x50, (byte) 0xBC,
			(byte) 0xA0, 0x00 };
	System.arraycopy(actDefault10, 0, app10.TACDefault, 0, actDefault10.length);
	byte[] aquirerId10 = new byte[] { (byte) 0x00, 0x00, 0x00, (byte) 0x12,
			0x34, 0x56 };
	System.arraycopy(aquirerId10, 0, app10.AcquierId, 0, aquirerId10.length);
	byte[] dDol10 = new byte[] { (byte) 0x03, (byte) 0x9F, 0x37, (byte) 0x04 };
	System.arraycopy(dDol10, 0, app10.dDOL, 0, dDol10.length);
	byte[] tDol10 = new byte[] { (byte) 0x0F, (byte) 0x9F, (byte) 0x02,
			(byte) 0x06, (byte) 0x5F, (byte) 0x2A, (byte) 0x02,
			(byte) 0x9A, (byte) 0x03, (byte) 0x9C, (byte) 0x01,
			(byte) 0x95, (byte) 0x05, (byte) 0x9F, (byte) 0x37, (byte) 0x04 };
	System.arraycopy(tDol10, 0, app10.dDOL, 0, tDol10.length);
	byte[] version10 = new byte[] { 0x00, 0x01 };
	System.arraycopy(version10, 0, app10.Version, 0, version10.length);	
	
	
	app11 = new EMV_APPLIST();
	byte[] appName11 = new String("Amex").getBytes();
	System.arraycopy(appName11, 0, app11.AppName, 0, appName11.length);
	//A00000 05 24 11 10
	byte[] aid11 = new byte[] { (byte) 0xA0, 0x00, 0x00, 0x00, 0x25, 0x01}; // pboc
	
	System.arraycopy(aid11, 0, app11.AID, 0, aid11.length);
	app11.AidLen = (byte) 0x05;
	app11.FloorLimit = 000000;
	app11.Threshold = 50000;
	app11.SelFlag = 0;
	app11.VelocityCheck = 1;
	byte[] actDenial11 = new byte[] { 0x04, 0x00, 0x00, 0x00, 0x00 };
	System.arraycopy(actDenial11, 0, app11.TACDenial, 0, actDenial11.length);
	byte[] actOnline11 = new byte[] { (byte) 0x00, 0x00, (byte) 0x00,
			(byte) 0x00, 0x00 };
	System.arraycopy(actOnline11, 0, app11.TACOnline, 0, actOnline11.length);
	byte[] actDefault11 = new byte[] { (byte) 0x00, 0x00, (byte) 0x00,
			(byte) 0x00, 0x00 };
	System.arraycopy(actDefault11, 0, app11.TACDefault, 0, actDefault11.length);
	byte[] aquirerId11 = new byte[] { (byte) 0x00, 0x00, 0x00, (byte) 0x00,
			0x00, 0x00 };
	System.arraycopy(aquirerId11, 0, app11.AcquierId, 0, aquirerId11.length);
	byte[] dDol11 = new byte[] { (byte) 0x03, (byte) 0x9F, 0x37, (byte) 0x04 };
	System.arraycopy(dDol11, 0, app11.dDOL, 0, dDol11.length);
	byte[] tDol11 = new byte[] { (byte) 0x0F, (byte) 0x9F, (byte) 0x02,
			(byte) 0x06, (byte) 0x5F, (byte) 0x2A, (byte) 0x02,
			(byte) 0x9A, (byte) 0x03, (byte) 0x9C, (byte) 0x01,
			(byte) 0x95, (byte) 0x05, (byte) 0x9F, (byte) 0x37, (byte) 0x04 };
	System.arraycopy(tDol11, 0, app11.dDOL, 0, tDol11.length);
	byte[] version11 = new byte[] { 0x00, 0x01 };
	System.arraycopy(version11, 0, app11.Version, 0, version11.length);	
	
	
	EMV_PARAM param = emv.getParameter();// �����ն˲���
	// set merchant id
	System.arraycopy(new String("111111111111111").getBytes(), 0,param.MerchId, 0, 15);
	// set term id
	System.arraycopy(new String("12345678").getBytes(), 0, param.TermId, 0,8);
	param.TerminalType=0x22;
	// set country code / currency code
	byte[] countryCode = new byte[] { 0x04, (byte) 0x80 };
	byte[] currencyCode = new byte[] { 0x04, (byte) 0x80 };
	System.arraycopy(countryCode, 0, param.CountryCode, 0, 2);
	System.arraycopy(currencyCode, 0, param.TransCurrCode, 0, 2);

	byte[] termCap = new byte[] { (byte) 0xE0, (byte) 0xF8, (byte) 0xC8 };
	System.arraycopy(termCap, 0, param.Capability, 0, 3);
	// param.ForceOnline=(byte)0x01;
	param.TransType = (byte) 0x00;
	emv.delAllApp();// ɾ������APP
	emv.addApp(app);
	emv.addApp(app1);
	emv.addApp(app2);// ���Ӧ��
	emv.addApp(app3);
	emv.addApp(app4);
	emv.addApp(app5);
	emv.addApp(app6);
	emv.addApp(app7);
	emv.addApp(app8);
	emv.addApp(app9);
	emv.addApp(app10);
	emv.addApp(app11);
	emv.setParameter(param);
	EMV_MCK_PARAM s=	emv.getMCKParam();
	 byte ucBypassPin    =s.ucBypassPin;
	 byte ucBatchCapture =s.ucBatchCapture;
	s.ucUseTermAIPFlg=1;
	 s.aucTermAIP [0]=0x08;
	 byte ucBypassAllFlg =s.ucBypassAllFlg;
	 emv.setMCKParam(s);
//	getMckParam();
	int ret;
	controller.setTransactionCounter();
	
	ret = emv.appSelect(0, controller.getTransactionCounter(), 1); // Ӧ��ѡ��Ӧ�ó�ʼ��
	TRACE.i("ret----------------"+ret);
	if (ret != EmvManager.EMV_OK) {
		Log.w(TAG, "appselect ret: " + ret);
		Bundle bundle = new Bundle();
		bundle.putString("status",String.format("app select error : %d", ret));
		if(ret==-6||ret==-9){
			ui.scrCls();
			ui.scrShowText("%P0000 Unsupported Chip"+ "%P1410" + "Swipe Card");
			controller.handleFlips(FlipView.SWIPE_CARD,"Please swipe card");
			//showText("Unsupported Chip"+ "%P1410" + "Swipe Card");
		return ret;	
		}else{
		showText("Transaction" + "%P1410" +"Declined");
		controller.handleFlips(FlipView.DECLINED, "Transaction Declined "+String.format("app select error : %d", ret));
		}
		return 0;
	}
	return ret;
}
private int emvProc() throws IOException, ProtoException, CommonException, EmvException, UIException {
	TRACE.i("emvProc----------------");
	// TODO Auto-generated method stub
	byte[] aids = emv.getTLVData((int) 0x9F06, DataModel.EncryptionMode.CLEAR,(byte) 0).data;
	String insertedaid = Utils.byte2HexStrUnFormatted(aids, 0, aids.length).trim().substring(0, 14);

	EMV_APPLIST appTemp=new EMV_APPLIST();
	
	if (insertedaid.equals(Utils.byte2HexStrUnFormatted(app6.AID, 0,app6.AID.length).trim().substring(0, 14)))
		emv.modFinalAppPara(app6);
	else  if (insertedaid.equals(Utils.byte2HexStrUnFormatted(app5.AID, 0,app5.AID.length).trim().substring(0, 14)))
		emv.modFinalAppPara(app5);
	else if (insertedaid.equals(Utils.byte2HexStrUnFormatted(app4.AID, 0,app4.AID.length).trim().substring(0, 14)))
		emv.modFinalAppPara(app4);
	else if (insertedaid.equals(Utils.byte2HexStrUnFormatted(app3.AID, 0,app3.AID.length).trim().substring(0, 14)))
		emv.modFinalAppPara(app3);
	else if (insertedaid.equals(Utils.byte2HexStrUnFormatted(app2.AID, 0,app2.AID.length).trim().substring(0, 14)))
		emv.modFinalAppPara(app2);
	else if (insertedaid.equals(Utils.byte2HexStrUnFormatted(app1.AID, 0,app1.AID.length).trim().substring(0, 14)))
		emv.modFinalAppPara(app1);
	else if (insertedaid.equals(Utils.byte2HexStrUnFormatted(app.AID, 0,app.AID.length).trim().substring(0, 14)))
		emv.modFinalAppPara(app);
	else if (insertedaid.equals(Utils.byte2HexStrUnFormatted(app7.AID, 0,app7.AID.length).trim().substring(0, 14)))
		emv.modFinalAppPara(app7);
	else if (insertedaid.equals(Utils.byte2HexStrUnFormatted(app8.AID, 0,app8.AID.length).trim().substring(0, 14)))
		emv.modFinalAppPara(app8);
	else if (insertedaid.equals(Utils.byte2HexStrUnFormatted(app9.AID, 0,app9.AID.length).trim().substring(0, 14)))
		emv.modFinalAppPara(app9);
	else if (insertedaid.equals(Utils.byte2HexStrUnFormatted(app10.AID, 0,app10.AID.length).trim().substring(0, 14)))
		emv.modFinalAppPara(app10);
	else if (insertedaid.equals(Utils.byte2HexStrUnFormatted(app11.AID, 0,app11.AID.length).trim().substring(0, 14)))
		emv.modFinalAppPara(app11);

	ui.scrCls();
	ui.scrShowText("%P1400%F1" + "INR:" + Misc.cents2Yuan(transAmt)+ "%P1410" + "Processing..");
	emv.readAppData(); // ��Ӧ����� //��Ӧ�����
	byte[] capIn;
	String insertedCapIn=null;
	try {
		capIn = emv.getTLVData((int) 0x8F, DataModel.EncryptionMode.CLEAR,(byte) 0).data;
		 insertedCapIn = Utils.byte2HexStrUnFormatted(capIn, 0,capIn.length);
		 emv.delAllCAPK();
			addCapk(insertedaid.substring(0, 10), insertedCapIn);
	} catch (Exception e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	

	
	int ret;
	if ((ret = emv.cardAuth()) != EmvManager.EMV_OK) {
		// �ѻ������֤
		Log.w(TAG, "cardAuth ret: " + ret);
		Bundle bundle = new Bundle();
		bundle.putString("status",String.format("card auth error : %d", ret));
		showText("Transaction Declined");
		controller.handleFlips(FlipView.DECLINED, "Transaction "+ "%P1410" + " Declined \n"+ String.format("card auth error : %d", ret));
	}

	// emv.setTLVData((int)0x9F03,String.valueOf("00").getBytes());
	emv.setTLVData(0x9F41,  util.inttobyte(controller.getTransactionCounter()));
	emv.setTLVData((int) 0x9F02,util.inttobyte(transAmt));
//	emv.setTLVData((int) 0x9F03,util.inttobyte(0));
/*	if(controller.getCashbackAmount()!=null&&!controller.getCashbackAmount().equalsIgnoreCase("0.00")&&!controller.getCashbackAmount().equals(""))
	emv.setTLVData((int) 0x9F03,util.inttobyte(Integer.parseInt(controller.getCashbackAmount().replace(".", ""))));
*/	try {
		ui.scrCls();
		ui.scrShowText("%P1400%F1" + "INR:" + Misc.cents2Yuan(transAmt)+ "%P1410" + "Processing..");
	} catch (UIException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	getMckParam();
	getTag(0x8D,"after read app Tag 8D value : ");
	int[] transResult = emv.startTrans(transAmt, 0);
	getTag(0x8D,"after start transaction :");
	getTag(0x9F1B,"Floor Limit : ");
	getTag(0x95,"TVR : ");
	getTag(0x9B,"9B TSI : ");
	getMckParam();
	// ��������
	StartTxn starttxn = null;
	if(transResult[0]<=7&&transResult[0]>=0)
	 starttxn=StartTxn.values()[transResult[0]];
	else{
		 starttxn=StartTxn.values()[8];
	}
	String message=starttxn.getName();
	if(transResult[0]==EmvManager.EMV_OK&&transResult[1]==EmvManager.EMV_AC_ARQC)
	startTxnTags();
	else if(transResult[0]!=EmvManager.EMV_OK){
		showText("Transaction"+ "%P1410" + " Declined"+message);
		controller.handleFlips(FlipView.DECLINED, "Transaction Declined \n"+message);
	}else if(transResult[1]==EmvManager.EMV_AC_TC){
		showText("Transaction "+ "%P1410" + "Approved");
		controller.handleFlips(FlipView.APPROVED, "Transaction offline Approved");
	}else if(transResult[1]==EmvManager.EMV_AC_AAC){
		//showText("Transaction "+ "%P1410" + "Declined");
		showText("%P1400"+"Transaction  "+"%P1410"+" Declined");
		controller.handleFlips(FlipView.DECLINED, "Transaction offline Declined ");
	}else{
		//showText("Transaction "+ "%P1410" + "Declined");
		showText("Transaction  "+"%P1410"  + " Declined");
		controller.handleFlips(FlipView.DECLINED, "Transaction Declined ");
	}
		
	
	return transResult[1];
}	
	private String getTag(int keyTag,String log){
		TRACE.i("getTag----------------");
		String tagvalue = null;
		try {
			DataModel.DataWithEncryptionMode tag=emv.getTLVData(keyTag, DataModel.EncryptionMode.CLEAR, (byte) 0);
			tagvalue = Utils.byte2HexStrUnFormatted(tag.data, 0,tag.data.length).toUpperCase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CommonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EmvException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tagvalue;
	} 
public void scriptUpdate(JSONObject jsonObject) {
	TRACE.i("scriptUpdate----------------");
	// TODO Auto-generated method stub
	getTag(0x8D,"before complete transaction :");
	String dataString = "";
	String responseCode="";
	int[] i;
	if (jsonObject.length() > 0) {
            try {
				Iterator<String> iter = jsonObject.keys();
				while(iter.hasNext()){
				String key = iter.next();
				if(key.equals("8A")){
					responseCode=jsonObject.getString(key);
					 controller.  setResponseCode(responseCode);
					emv.setTLVData(0x8a, util.hexStringToBytes(jsonObject.getString(key)));
					getTag(0x8A,"before 8A after set 8A transaction :");	
				}else if(key.equals("91")){
					emv.setTLVData(0x91, util.hexStringToBytes(jsonObject.getString(key)));
					getTag(0x91,"before 91 after set 91 transaction :");
				}else if(key.equals("71")||key.equals("72")) {
					String value=jsonObject.getString(key);
					dataString+=key+String.format("%02X", value.length()/2)+value;	
					}
				}
				
				getTag(0x8A," 8A before complete transaction :");
				DataModel.DataWithEncryptionMode tag=emv.getTLVData(0x8d, DataModel.EncryptionMode.SENSITIVE_CIPHER_DUKPTDES, (byte) 3);
				String	tagvalue = Utils.byte2HexStrUnFormatted(tag.data, 0,tag.data.length).toUpperCase();
					if(responseCode.equals("3030"))
				i=	emv.completeTrans(EmvManager.EMV_ONLINE_APPROVE, util.hexStringToBytes(dataString));
					else
				i=	emv.completeTrans(EmvManager.EMV_ONLINE_DENIAL, util.hexStringToBytes(dataString));
					for (int j = 0; j < i.length; j++) {
					}
					if (i[1] == EmvManager.EMV_AC_TC&&i[0]==EmvManager.EMV_OK) {
						updateTcTags();
						showText("Please wait..  " + "%P1410" + " EMV Update..");
						controller.getTranscationMap().put("tcResult", "1");
						controller.handleTransaction(PostType.UPDATE_TC);
					}else if(i[1] == EmvManager.EMV_AC_AAC) {
						updateTcTags();
						controller.getTranscationMap().put("tcResult", "0");
						controller.handleTransaction(PostType.UPDATE_TC);
						showText("Declined  " + "%P1410" + "  by card");
						controller.handleFlips(FlipView.DECLINED,"Transaction Declined \n"+controller.getEmvMsg());
						
					}else{
						showText("Please wait..  " + "%P1410" + " EMV Update..");
						showText("Transaction  " + "%P1410" + " Declined");
						controller.handleFlips(FlipView.DECLINED);
						updateTcTags();
						controller.getTranscationMap().put("tcResult", "0");
						controller.handleTransaction(PostType.UPDATE_TC);
					}
					
						return;
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EmvException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ProtoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CommonException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
       // setEmvMsg(type.getMessage());
    //    paxEmvTxnMgr.scriptUpdate(util.hexStringToBytes(dataString));
       
    }
}
	public void scriptUpdate(byte[] script) {
		try {
			int[] i = emv.completeTrans(EmvManager.EMV_ONLINE_APPROVE, script);
			
			if (i[1] == EmvManager.EMV_OK) {
				updateTcTags();
				showText("Please wait..  " + "%P1410" + " EMV Update..");
				
				txnMap.put("tcResult", "1");
				controller.handleTransaction(PostType.UPDATE_TC);
				return;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CommonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		showText("Transaction  " + "%P1410" + " Declined");
		txnMap.put("tcResult", "0");
		controller.handleTransaction(PostType.UPDATE_TC);
	}

	private void addCapk(String insertedaid, String insertedCapIn) {
		TRACE.i("addCapk----------------");
		Log.e("CAP AID", insertedaid);
		List<EMV_CAPK> list = new ArrayList<EMV_CAPK>();
		InputStreamReader myInput = null;
		try {
			myInput = new InputStreamReader(activity.getAssets().open(
					"pax_MIURA CAPKEYS.txt"));

			// FileReader fr = new FileReader("capkeys.cfg");
			BufferedReader br = new BufferedReader(myInput);

			String sCurrentLine;
			EMV_CAPK cap = null;
			byte[] temp;
			while ((sCurrentLine = br.readLine()) != null) {
				if (sCurrentLine.startsWith("#")) {
				} else if (sCurrentLine.startsWith("RID")) {
					cap = new EMV_CAPK();
					temp = util.hexStringToBytes(sCurrentLine.substring(
							sCurrentLine.indexOf("=") + 1).trim());
					System.arraycopy(temp, 0, cap.RID, 0, temp.length);
				} else if (sCurrentLine.startsWith("KEY_ID")) {
					cap.KeyID = (byte) Integer.parseInt(
							sCurrentLine.substring(
									sCurrentLine.indexOf("=") + 1).trim(), 16);
				} else if (sCurrentLine.startsWith("HASH_IND")) {
					cap.HashInd = (byte) Integer.parseInt(sCurrentLine
							.substring(sCurrentLine.indexOf("=") + 1).trim(),
							16);
				} else if (sCurrentLine.startsWith("EXP_LEN")) {
					cap.ExpLen = (byte) Integer.parseInt(sCurrentLine
							.substring(sCurrentLine.indexOf("=") + 1).trim(),
							16);
				} else if (sCurrentLine.startsWith("MOD_LEN")) {
					cap.ModulLen = (byte) Integer.parseInt(sCurrentLine
							.substring(sCurrentLine.indexOf("=") + 1).trim());
				} else if (sCurrentLine.startsWith("ARITH_INDEX")) {
					cap.ArithInd = (byte) Integer.parseInt(sCurrentLine
							.substring(sCurrentLine.indexOf("=") + 1).trim(),
							16);
				} else if (sCurrentLine.startsWith("MODULUS")) {
					temp = util.hexStringToBytes(sCurrentLine.substring(
							sCurrentLine.indexOf("=") + 1).trim());
					System.arraycopy(temp, 0, cap.Modul, 0, temp.length);
				} else if (sCurrentLine.startsWith("EXPONENT")) {
					temp = util.hexStringToBytes(sCurrentLine.substring(
							sCurrentLine.indexOf("=") + 1).trim());
					System.arraycopy(temp, 0, cap.Exp, 0, temp.length);
				} else if (sCurrentLine.startsWith("EXP_DATE")) {
					temp = util.hexStringToBytes(sCurrentLine.substring(
							sCurrentLine.indexOf("=") + 1).trim());
					System.arraycopy(temp, 0, cap.ExpDate, 0, temp.length);
				} else if (sCurrentLine.startsWith("CHECKSUM")) {
					temp = util.hexStringToBytes(sCurrentLine.substring(
							sCurrentLine.indexOf("=") + 1).trim());
					System.arraycopy(temp, 0, cap.CheckSum, 0, temp.length);
					list.add(cap);
				} else if (sCurrentLine.trim().equals("")) {
				}
			}
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Iterator<EMV_CAPK> iterator = list.iterator();
		for (Iterator<EMV_CAPK> iterator2 = list.iterator(); iterator2.hasNext();) {
			EMV_CAPK emv_CAPK = iterator2.next();
			try {
				String s = String.format("%02x", emv_CAPK.KeyID);
				if (Utils.byte2HexStrUnFormatted(emv_CAPK.RID, 0,
						emv_CAPK.RID.length).equalsIgnoreCase(insertedaid)
						&& s.equalsIgnoreCase(insertedCapIn))
					emv.addCAPK(emv_CAPK);
			} catch (EmvException e) {

				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ProtoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CommonException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	private Synchronizer synchronizer = new Synchronizer();

	class Synchronizer {
		private boolean wakeupNow = false;

		public synchronized void dontWakeup() {
			wakeupNow = false;

		}

		public synchronized void wakeItUp() {
			wakeupNow = true;
			notifyAll();

		}

		public synchronized void waitForResult(int timeout)
				throws InterruptedException {
			while (!wakeupNow) {
				wait(timeout);
			}
		}
	}

	@SuppressWarnings("static-access")
	private void startTxnTags() {
		int temp[] = { 0x9F37, 0x95, 0x9A,0x8a, 0x9C, 0x82, 0x84, 0x9F1A, 0x9f1e,
				0x50, 0x4f, 0x9b, 0x5F2A, 0x5f20, 0x5f28, 0x5f30, 
				0x5f24, 0x5f34, 0x9F02, 0x9F03, 0x9f06, 0x9f07, 0x9f08, 0x9f09,
				0x9f10, 0x9f11, 0x9f12, 0x9f1c, 0x9f26, 0x9f27, 0x9f33, 0x9f34,
				0x9f35, 0x9f36, 0x9f37, 0x9f41,0x57 }; // These tags's TLVs can get
													// by this way
		// 0x9f09,
		Map<String, String> map = new HashMap<String, String>();
		Integer iTag;
		String s = "";
		map.put("9F03", "000000000000");
		String ksn2 = null;
		for (int i = 0; i < temp.length; i++) {
			String key, value;
			iTag = new Integer(temp[i]);
			key = iTag.toHexString(temp[i]).toUpperCase();
			DataModel.DataWithEncryptionMode Tag = null;
			try {
				Tag = emv.getTLVData(temp[i], DataModel.EncryptionMode.SENSITIVE_CIPHER_DUKPTDES, (byte) 3);
				/*if (!key.equals("5A"))
					Tag = emv.getTLVData(temp[i],EncryptionMode.SENSITIVE_CIPHER_DUKPTDES, (byte) 3);
				else
					Tag = emv.getTLVData(temp[i], EncryptionMode.CLEAR,(byte) 0);*/
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ProtoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CommonException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EmvException e) {
				e.printStackTrace();

			}
			Log.i(TAG, "--------------------------");
			TRACE.i(TAG+"====="+ key);
			if (key.equalsIgnoreCase("57")) {
				if (Tag.ksn != null) {
					ksn2 = Utils.byte2HexStr(Tag.ksn, 0, Tag.ksn.length)
							.replace(" ", "").toUpperCase();
				}
				key = "DFAE02";
			}
			s += iTag.toHexString(temp[i]) + "=";
			String tagvalue = null;
			if ((Tag != null) && (Tag.data != null)) {
				tagvalue = Utils.byte2HexStrUnFormatted(Tag.data, 0,Tag.data.length).toUpperCase();
				map.put(key, tagvalue);
				s += tagvalue + "|";
				Log.i(TAG, tagvalue);
			}
			Log.i(TAG, "--------------------------");
		}
		if (ksnPin != null) {
			map.put("DFAE05", ksnData.toString());
			map.put("DFAE04", pinBlockData.toString());
		}
		if (ksn2 != null) {
			map.put("DFAE03", ksn2);
		}
		map.put("transactionMode", "1");
	//	map.put("9F03", "000000000000");
		// map.put("9F09","002");
		map.put("terminalTxnResult", "1");
		controller.setTranscationMap(map);
		controller.handleTransaction();

		// Log.i(TAG,s);
	}

	private void updateTcTags() {
		TRACE.i("updateTcTags----------------");
		int temp[] = { 0x95, 0x9A,0x8A, 0x9C, 0x50, 0x9b, 0x5F2A, 0x5f20, 0x5f28,
				0x5f24, 0x9F02, 0x9f07, 0x9f1e, 0x9f33, 0x9f34, 0x9f35, }; // These
		Map<String, String> map = new HashMap<String, String>();
		Integer iTag;

		for (int i = 0; i < temp.length; i++) {
			String key;

			iTag = new Integer(temp[i]);
			key = Integer.toHexString(temp[i]).toUpperCase();

			DataModel.DataWithEncryptionMode Tag = null;
			try {

				Tag = emv.getTLVData(temp[i], DataModel.EncryptionMode.CLEAR, (byte) 0);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ProtoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CommonException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				e.printStackTrace();

			} catch (EmvException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i(TAG, "--------------------------");
			Log.i(TAG, key);
			String tagvalue = null;
			if ((Tag != null) && (Tag.data != null)) {
				tagvalue = Utils.byte2HexStrUnFormatted(Tag.data, 0,Tag.data.length).toUpperCase();
				map.put(key, tagvalue);
				Log.i(TAG, tagvalue);
			}
			Log.i(TAG, "--------------------------");
			map.put(TRANSACTION_MODE_KEY, MODE_EMV_TRANSACTION);
			controller.setTranscationMap(map);
		}
	}

	/*private void loadIdleScreen() throws IOException, ProtoException,
			BaseSystemException, UIException, CommonException {
		ui.scrCls();
		if (TermSpecific.getInstance(activity).isScr12864()) {
			ui.scrShowText("%P1400%F1" + "MOSAMBEE"); // SHOW THANKS
		} else {
			try {
				ui.scrProcessImage("idle.png", UIManager.UI_IMAGE_TYPE_PNG,
						UIManager.UI_PROCESS_IMAGE_CMD_LOAD, 0, 0);
				ui.scrProcessImage("idle.png", UIManager.UI_IMAGE_TYPE_PNG,
						UIManager.UI_PROCESS_IMAGE_CMD_DISPLAY, 0, 0);
			} catch (UIException ue) {
				// TODO: handle exception
				if (ue.exceptionCode != -0xffff) { // swallow unsupported
													// exception
					throw ue;
				}
			}
		}
	}*/

	public void sendOnlinePin() {
		// TODO Auto-generated method stub

		// ui.scrShowText("ENTER PIN: \n ");
		try {
			ui.scrCls();
			ui.scrShowText("%P1400%F1" + "INR" + ":"+ Misc.cents2Yuan(transAmt) + "%P1410"+ activity.getString(R.string.waitingForInputPin) + ":"+ "%P1420");
			pinBlockArray = ped.pedGetPinDukpt((byte) 4, "4,5,6","".getBytes(), PedManager.PED_PINBLOCK_ISO9564_0,30000);
			pinBlockData = new StringBuilder();
			ksnData = new StringBuilder();
			pinBlock = pinBlockArray.pinBlockOut;
			ksnPin = pinBlockArray.ksnOut;
			for (int i = 0; i < pinBlock.length; i++) {
				pinBlockData.append(String.format("%02X", pinBlock[i]));
			}
			for (int i = 0; i < ksnPin.length; i++) {
				ksnData.append(String.format("%02X", ksnPin[i]));
			}
			ui.scrCls();
			ui.scrShowText("%P1400%F1" + "INR" + ":"+ Misc.cents2Yuan(transAmt) + "%P1410" + "Processing..");
		} catch (UIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CommonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		controller.handleFlips(FlipView.CONNECTING);
	}

	public void continueTxn() {
		// TODO Auto-generated method stub

	}
	private void getMckParam() {
		TRACE.i("getMckParam----------------");
		// TODO Auto- method stub
			try {
				EMV_MCK_PARAM s=	emv.getMCKParam();
				 byte ucBypassPin    =s.ucBypassPin;
				 byte ucBatchCapture =s.ucBatchCapture;
				 byte ucUseTermAIPFlg=s.ucUseTermAIPFlg;
				 byte[] aucTermAIP   =s.aucTermAIP;
				 byte ucBypassAllFlg =s.ucBypassAllFlg;
			} catch (EmvException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ProtoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CommonException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}
	
	private void terminate() {
        running = false;
    }
    private Handler mHandler = new Handler();
	 private volatile boolean running = true;
		public void close() {
			showText("Transaction"+  "%P1410"  +   "  Aborted");
			  try {
				if (swipeThread != null) { 
					 terminate();
					  Thread.sleep(50);
					 Thread.currentThread().interrupt();
					mHandler.removeCallbacks(swipeRunnable);
					swipeThread = null; 
					
					//Nitu Comment here pax device force close when we abort issuse resolved
					 // base.closeConnection();
					  }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	public void fallback(){
		try {
			String record="",maskedPan;
			while (!Thread.interrupted()) {
				Thread.sleep(500);
				if (mag.magSwiped()) {

					MagManager.TrackDataOutput track = mag.magRead(DataModel.EncryptionMode.SENSITIVE_CIPHER_DUKPTDES,(byte)3);
					String str1 = new String(track.track1, "iso-8859-1");
					String str2 = new String();
					String str3 = new String(track.track3, "iso-8859-1");
					byte[] str4 = track.ksn;
					String str5 = track.maskedPan;
					byte[] str6 = track.serviceCode;
					StringBuilder ksn = new StringBuilder();
					String serviceCode = new String();
					for (int i = 0; i < track.track2.length; i++) {
						str2 += String.format("%02X", track.track2[i]);
					}
					record += "Track1: " + str1 + "\n\n";
					record += "Track2: " + str2 + "\n\n";
					record += "Track3: " + str3 + "\n\n";
					record += "KSN: ";
					maskedPan = str5;
					for (int i = 0; i < str4.length; i++) {
						ksn.append(String.format("%02X", str4[i]));
					}
					record += ksn + "\n\n";
					record += "MASKEDPAN: " + str5 + "\n\n";
					record += "ServiceCode: ";
					  for(int i=0;i<str6.length;i++){ 
						  serviceCode +=str6[i]+" "; 
						  }
					serviceCode = new String(str6);
					record += "\n\n" + serviceCode;
					trackData = new String[5];
					trackData[0] = str1;
					trackData[1] = str2;
					trackData[2] = ksn.toString();
					cardType = MAG_CARD;

					  if(serviceCode.startsWith("2")||serviceCode.startsWith("6")){
						  controller.handleFlips(FlipView.INSERT_CARD);
						  //ui.scrShowText("%P1400%F0" +"Chip enabled card"+  "%P1410" + "Please insert card");
						  ui.scrCls();
						  ui.scrShowText("%P1400%F0" +"Chip enabled card"+"%P1410"+"%F0"+"Please insert card");
						  cardType=-1;
					  		}
					if(cardType!=-1)
					break;
				}
				if (emvDetect()) {
					cardType = EMV_CARD;
					break;
				}
				
			}
			mag.magClose();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MagException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CommonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IccException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void closeReader() {
		// TODO Auto-generated method stub
		controller.handleFlips(FlipView.DECLINED);
	}
}

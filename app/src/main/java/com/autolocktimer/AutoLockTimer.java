package com.autolocktimer;

import com.activity.OnAutoLockScreen;
import com.data.TRACE;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


public class AutoLockTimer implements OnAutoLockListener {
    private static int lockTime;
    private Timer timer;
    private AutoLockTimerTask autoLock;

    private OnAutoLockScreen lockScreen;

    private static AutoLockTimer autoLockTime = new AutoLockTimer();

    private AutoLockTimer() {}

    public static AutoLockTimer getAutoLockTimer(int time) {
        lockTime = time;
        TRACE.i("AutoLockTimer--- "+time);
        return autoLockTime;
    }

    @Override
    public void setOnAutoLockListener(OnAutoLockScreen lockScreen) {
        this.lockScreen = lockScreen;
    }

    @Override
    public void onAutoLockTime() {
        if(lockScreen != null) {
            lockScreen.onAutoLock();
        }
    }

    @Override
    public void startTimer() {
        timer = new Timer();
        autoLock = new AutoLockTimerTask();
        scheduleTime();
    }

    @Override
    public void resetTimer() {
        clearLock();
        autoLock = new AutoLockTimerTask();
        scheduleTime();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = sdf.format(c.getTime());
        TRACE.i("TIME IN AutoLockTimer  " + strDate);
    }

    @Override
    public void stopTimer() {
        clearLock();
        lockScreen = null;
    }

    private void clearLock() {
        if (autoLock != null) {
            autoLock.cancel();
            autoLock = null;
        }
    }

    private void scheduleTime() {
        //TRACE.i("lock time" + lockTime);
        timer.schedule(autoLock, lockTime * 1000L);
    }

    class AutoLockTimerTask extends TimerTask {
        public void run() {
            onAutoLockTime();
        }
    }
}

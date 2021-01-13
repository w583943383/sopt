package com.crgt.crash;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import com.crgt.crash.compat.ActivityKillerV15_V20;
import com.crgt.crash.compat.ActivityKillerV21_V23;
import com.crgt.crash.compat.ActivityKillerV24_V25;
import com.crgt.crash.compat.ActivityKillerV26;
import com.crgt.crash.compat.ActivityKillerV28;
import com.crgt.crash.compat.IActivityKiller;


public class ActivityThreadCallback implements Handler.Callback {

    final static int LAUNCH_ACTIVITY = 100;
    final static int PAUSE_ACTIVITY = 101;
    final static int PAUSE_ACTIVITY_FINISHING = 102;
    final static int STOP_ACTIVITY_HIDE = 104;
    final static int RESUME_ACTIVITY = 107;
    final static int DESTROY_ACTIVITY = 109;


    private final Handler mHandler;
    private static IActivityKiller sActivityKiller;

    public ActivityThreadCallback() {
        initActivityKiller();
        this.mHandler = getHandler(getActivityThread());
    }


    /**
     * 替换ActivityThread.mH.mCallback，实现拦截Activity生命周期，直接忽略生命周期的异常的话会导致黑屏，目前
     * 会调用ActivityManager的finishActivity结束掉生命周期抛出异常的Activity
     */
    private static void initActivityKiller() {
        //各版本android的ActivityManager获取方式，finishActivity的参数，token(binder对象)的获取不一样
        if (Build.VERSION.SDK_INT >= 28) {
            sActivityKiller = new ActivityKillerV28();
        } else if (Build.VERSION.SDK_INT >= 26) {
            sActivityKiller = new ActivityKillerV26();
        } else if (Build.VERSION.SDK_INT == 25 || Build.VERSION.SDK_INT == 24) {
            sActivityKiller = new ActivityKillerV24_V25();
        } else if (Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT <= 23) {
            sActivityKiller = new ActivityKillerV21_V23();
        } else if (Build.VERSION.SDK_INT >= 15 && Build.VERSION.SDK_INT <= 20) {
            sActivityKiller = new ActivityKillerV15_V20();
        } else if (Build.VERSION.SDK_INT < 15) {
            sActivityKiller = new ActivityKillerV15_V20();
        }
    }



    private static Object getActivityThread() {
        Object thread = null;
        try {
            thread = android.app.ActivityThread.currentActivityThread();
        } catch (Throwable t) {
            t.printStackTrace();
            try {
                thread = Reflection.getStaticFieldValue(android.app.ActivityThread.class, "sCurrentActivityThread");
            } catch (final Throwable t2) {
                t.printStackTrace();
            }
        }
        return thread;
    }


    private static Handler getHandler(final Object thread) {
        Handler handler;
        if (thread == null) {
            return null;
        }
        if (null != (handler = Reflection.getFieldValue(thread, "mH"))) {
            return handler;
        }
        if (null != (handler = Reflection.invokeMethod(thread, "getHandler"))) {
            return handler;
        }

        try {
            if (null != (handler = Reflection.getFieldValue(thread, Class.forName("android.app.ActivityThread$H")))) {
                return handler;
            }
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public boolean handleMessage(Message msg) {
        if (Build.VERSION.SDK_INT >= 28) {//android P 生命周期全部走这
            final int EXECUTE_TRANSACTION = 159;
            if (msg.what == EXECUTE_TRANSACTION) {
                try {
                    mHandler.handleMessage(msg);
                } catch (Throwable throwable) {
                    sActivityKiller.finishLaunchActivity(msg);
                    notifyException(throwable);
                }
                return true;
            }
            return false;
        }
        switch (msg.what) {
            case LAUNCH_ACTIVITY:// startActivity--> activity.attach  activity.onCreate  r.activity!=null  activity.onStart  activity.onResume
                try {
                    mHandler.handleMessage(msg);
                } catch (Throwable throwable) {
                    sActivityKiller.finishLaunchActivity(msg);
                    notifyException(throwable);
                }
                return true;
            case RESUME_ACTIVITY://回到activity onRestart onStart onResume
                try {
                    mHandler.handleMessage(msg);
                } catch (Throwable throwable) {
                    sActivityKiller.finishResumeActivity(msg);
                    notifyException(throwable);
                }
                return true;
            case PAUSE_ACTIVITY_FINISHING://按返回键 onPause
                try {
                    mHandler.handleMessage(msg);
                } catch (Throwable throwable) {
                    sActivityKiller.finishPauseActivity(msg);
                    notifyException(throwable);
                }
                return true;
            case PAUSE_ACTIVITY://开启新页面时，旧页面执行 activity.onPause
                try {
                    mHandler.handleMessage(msg);
                } catch (Throwable throwable) {
                    sActivityKiller.finishPauseActivity(msg);
                    notifyException(throwable);
                }
                return true;
            case STOP_ACTIVITY_HIDE://开启新页面时，旧页面执行 activity.onStop
                try {
                    mHandler.handleMessage(msg);
                } catch (Throwable throwable) {
                    sActivityKiller.finishStopActivity(msg);
                    notifyException(throwable);
                }
                return true;
            case DESTROY_ACTIVITY:// 关闭activity onStop  onDestroy
                try {
                    mHandler.handleMessage(msg);
                } catch (Throwable throwable) {
                    notifyException(throwable);
                }
                return true;
        }
        return false;
    }

    private static void notifyException(Throwable throwable) {
        if (CrashOpt.isSafeMode()) {
            CrashOpt.getExceptionHandler().bandageExceptionHappened(throwable);
        } else {
            CrashOpt.getExceptionHandler().uncaughtExceptionHappened(Looper.getMainLooper().getThread(), throwable);
            CrashOpt.safeMode();
        }
    }


    public boolean hook() {
        return Reflection.setFieldValue(this.mHandler, "mCallback", this);

    }
}

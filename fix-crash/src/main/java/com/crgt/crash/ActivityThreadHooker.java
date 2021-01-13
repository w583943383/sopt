package com.crgt.crash;



public class ActivityThreadHooker {


    private static final String[] ignorePackages = new String[]{};

    private static boolean isHooked = false;

    public static final String TAG = "hook_mh";

    public static void hook(final String ignorePackagers) {
        if (isHooked) {
            return;
        }
        try {
            final ActivityThreadCallback activityThreadCallback = new ActivityThreadCallback();
            if (!(isHooked = activityThreadCallback.hook())) {
            }
        } catch (final Throwable t){
            t.printStackTrace();
        }


    }
}

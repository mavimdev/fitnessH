package com.mavimdev.fitnessh.fragment;

import android.content.Context;

public interface UpdateClassesInterface {
    /**
     * do the update to other classes list
     */
    void refreshOtherClasses(Context context);

    /**
     * do the update to the current classes list
     */
    void refreshCurrentClasses(Context context);

}

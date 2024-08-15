package com.ts.smshubconnect

import android.os.PowerManager
import android.os.PowerManager.WakeLock


internal class SleepLock

private constructor() {
    private var myobject: PowerManager.WakeLock? = null

    fun getMyWakeLock(): PowerManager.WakeLock?{
        return myobject
    }

    fun setMyWakeLock(obj: WakeLock?) {
        myobject = obj
    }

    companion object {
        private var dataObj: SleepLock? = null
        val instance: SleepLock
            get() {
                if (dataObj == null){
                    dataObj = SleepLock()
                }
                return dataObj!!
            }

    }
}
package com.jackmanu.scplusplus;

/**
 * Created by dougmcintosh on 10/2/16.
 */
import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class MyBackupHelper extends BackupAgentHelper {
    // The names of the SharedPreferences groups that the application maintains.  These
    // are the same strings that are passed to getSharedPreferences(String, int).
    public static final String PREFS = "jackmanu.scplusplusBackup";

    // An arbitrary string used within the BackupAgentHelper implementation to
    // identify the SharedPreferenceBackupHelper's data.
    static final String MY_PREFS_BACKUP_KEY = "jackmanu.scplusplusBackup";

    // Simply allocate a helper and install it
    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper helper =
                new SharedPreferencesBackupHelper(this, PREFS);
        addHelper(MY_PREFS_BACKUP_KEY, helper);
    }
}

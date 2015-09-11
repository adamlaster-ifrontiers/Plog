/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.lastsoft.plog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.lastsoft.plog.util.NotificationFragment;
import com.lastsoft.plog.util.PostMortemReportExceptionHandler;
import com.lastsoft.plog.util.SyncPlaysTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;

public class SettingsFragment extends PreferenceFragment {

    private Preference mImportPreference, mExportPreference, mSendDebugPreference, mSyncPlays;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        mExportPreference = getPreferenceManager().findPreference("export_db");
        mExportPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                exportDB();
                return false;
            }
        });
        mImportPreference =  getPreferenceManager().findPreference("import_db");
        String backupDBPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SRX_export.db";
        File backupDB = new File(backupDBPath);
        if (backupDB.exists()) {
            mImportPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    importDB();
                    return false;
                }
            });
        }else{
            mImportPreference.setEnabled(false);
        }

        mSendDebugPreference = getPreferenceManager().findPreference("send_debug");
        mSendDebugPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                sendDebug();
                return false;
            }
        });

        mSyncPlays = getPreferenceManager().findPreference("sync_plays");
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        long defaultPlayer = app_preferences.getLong("defaultPlayer", -1);
        if (defaultPlayer >= 0) {
            mSyncPlays.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    syncPlays();
                    return false;
                }
            });
        }else{
            mSyncPlays.setEnabled(false);
        }
    }

    public void syncPlays(){
        SyncPlaysTask loadPlays = new SyncPlaysTask(getActivity());
        try {
            loadPlays.execute();
        } catch (Exception ignored) {

        }
    }

    public void sendDebug(){
        String ss = "\n\n\n\n-------- The following information is used to aid our support technicians in helping you --------\n\n";
        ss = ss + PostMortemReportExceptionHandler.getDeviceEnvironment();
        String t = "";
        t = "--------- Activity Stack Trace ---------\n";
        Process mLogcatProc = null;
        BufferedReader reader = null;
        try
        {
            mLogcatProc = Runtime.getRuntime().exec(new String[]
                    {"logcat", "-d", "AndroidRuntime:E *:V" });

            reader = new BufferedReader(new InputStreamReader(mLogcatProc.getInputStream()));
            String line;
            final StringBuilder log = new StringBuilder();
            String separator = System.getProperty("line.separator");
            int ids= android.os.Process.myPid();
            while ((line = reader.readLine()) != null)
            {
                if (line.contains(""+ids)){
                    log.append(line);
                    log.append(separator);
                }

            }
            t += log;
            // do whatever you want with the log.  I'd recommend using Intents to create an email
        }

        catch (IOException e)
        {
        }

        finally
        {
            if (reader != null)
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                }
        }

        t += "----------------------------------------\n\n";
        t += ss;

        //send feedback
        Intent prefIntent44 = new Intent();
        prefIntent44.setAction(Intent.ACTION_SENDTO);
        prefIntent44.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        prefIntent44.setData(Uri.parse("mailto:aelaster@gmail.com"));
        prefIntent44.putExtra("android.intent.extra.SUBJECT", getString(R.string.app_name) + " Android App Version " + PostMortemReportExceptionHandler.getVersion() + " - Debug");
        prefIntent44.putExtra("android.intent.extra.TEXT", t);
        startActivity(prefIntent44);
    }

    public void importDB(){
        try{
            File oldDb = getActivity().getDatabasePath("SRX.db");
            File newDb = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SRX_export.db");
            if (newDb.exists()) {
                if(oldDb.exists()){

                }
                else{
                    //This'll create the directories you wanna write to, so you
                    //can put the DB in the right spot.
                    oldDb.getParentFile().mkdirs();
                }
                Log.d("V1", "importing database");
                FileInputStream src_input = new FileInputStream(newDb);
                FileOutputStream dst_input = new FileOutputStream(oldDb);
                FileChannel src = src_input.getChannel();
                FileChannel dst = dst_input.getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                src_input.close();
                dst_input.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exportDB(){
        try{
            File currentDB = getActivity().getDatabasePath("SRX.db");
            String backupDBPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SRX_export.db";
            File backupDB = new File(backupDBPath);

            if (currentDB.exists()) {
                FileInputStream src_input = new FileInputStream(currentDB);
                FileOutputStream dst_input = new FileOutputStream(backupDB);
                FileChannel src = src_input.getChannel();
                FileChannel dst = dst_input.getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                src_input.close();
                dst_input.close();
                mImportPreference.setEnabled(true);
            }
            notifyUser(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notifyUser(int notificationId){
        NotificationFragment newFragment = new NotificationFragment().newInstance(notificationId);
        newFragment.show(getFragmentManager(), "notifyUser");
    }
}

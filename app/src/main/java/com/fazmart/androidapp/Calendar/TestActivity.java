/*
 * Copyright (C) 2011 Chris Gao <chris@exina.net>
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
package com.fazmart.androidapp.Calendar;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import com.fazmart.androidapp.R;

public class TestActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener{
	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		addPreferencesFromResource(R.xml.test);
		
		findPreference("show").setIntent(new Intent(Intent.ACTION_VIEW).setDataAndType(null, CalendarActivity.MIME_TYPE));
		findPreference("pick").setOnPreferenceClickListener(this);
		findPreference("about").setOnPreferenceClickListener(this);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode==RESULT_OK) {
			int year = data.getIntExtra("year", 0);
			int month = data.getIntExtra("month", 0);
			int day = data.getIntExtra("day", 0);
			final Calendar dat = Calendar.getInstance();
			dat.set(Calendar.YEAR, year);
			dat.set(Calendar.MONTH, month);
			dat.set(Calendar.DAY_OF_MONTH, day);
			
			SimpleDateFormat format = new SimpleDateFormat("yyyy MMM dd");
			Toast.makeText(TestActivity.this, format.format(dat.getTime()), Toast.LENGTH_LONG).show();
					
		}
	}
	
	public boolean onPreferenceClick(Preference  preference) {
		String key = preference.getKey();
		if(key.equals("pick")) {
			startActivityForResult(new Intent(Intent.ACTION_PICK).setDataAndType(null, CalendarActivity.MIME_TYPE), 100);
		} else if(key.equals("about")) {
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.app_name).setMessage("http://code.google.com/p/android-calendar-view/\n\nBy Chris Gao<chris@exina.net>").create().show();
		}
		return true;
	}
}

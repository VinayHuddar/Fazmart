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

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fazmart.androidapp.Calendar.*;
import com.fazmart.androidapp.Calendar.CalendarView;
import com.fazmart.androidapp.R;

public class CalendarDialog extends DialogFragment implements CalendarView.OnCellTouchListener {
	public static final String MIME_TYPE = "vnd.android.cursor.dir/vnd.exina.android.calendar.date";
	CalendarView mView = null;
	TextView mHit;
	Handler mHandler = new Handler();

    /** Called when the activity is first created. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
							   Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		View v = inflater.inflate(R.layout.calendar_main, container, false);
		mView = (CalendarView)v.findViewById(R.id.calendar);
		mView.setOnCellTouchListener(this);

        this.getDialog().setTitle("Select Delivery Date");
		return mView;
    }

	public void onTouch(com.fazmart.androidapp.Calendar.Cell cell) {
		int year  = mView.getYear();
		int month = mView.getMonth();
		int day   = cell.getDayOfMonth();

		// FIX issue 6: make some correction on month and year
		if(cell instanceof CalendarView.GrayCell) {
			// oops, not pick current month...
			if (day < 15) {
				// pick one beginning day? then a next month day
				if(month==11)
				{
					month = 0;
					year++;
				} else {
					month++;
				}

			} else {
				// otherwise, previous month
				if(month==0) {
					month = 11;
					year--;
				} else {
					month--;
				}
			}
		}

		Intent ret = new Intent();
		ret.putExtra("year", year);
		ret.putExtra("month", month);
		ret.putExtra("day", day);

		if(mView.firstDay(day))
			mView.previousMonth();
		else if(mView.lastDay(day))
			mView.nextMonth();
		else
			return;

		mHandler.post(new Runnable() {
			public void run() {
				Toast.makeText(getActivity(), DateUtils.getMonthString(mView.getMonth(), DateUtils.LENGTH_LONG) + " "+mView.getYear(), Toast.LENGTH_SHORT).show();
			}
		});
	}

    
}
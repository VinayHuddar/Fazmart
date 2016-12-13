package com.fazmart.androidapp.View.Checkout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.fazmart.androidapp.Controller.DeliveryDataController;
import com.fazmart.androidapp.Model.UserAccountData;
import com.fazmart.androidapp.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class DeliveryDataActivity extends FragmentActivity {
    DeliveryDataController mController;
    boolean mComingFrom_onCreate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_info);

        mController = new DeliveryDataController(this, (UserAccountData.GetInstance().GetAccountData(this) != null));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mComingFrom_onCreate)
            mComingFrom_onCreate = false;
        else
            mController.EventBusRegister();
    }

    @Override
    protected void onPause () {
        mController.EventBusUnRegister();
        super.onPause();
    }

    // This function is used to get the results from the calender, which is not being used currently
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if(resultCode==RESULT_OK) {
        int year = data.getIntExtra("year", 0);
        int month = data.getIntExtra("month", 0);
        int day = data.getIntExtra("day", 0);
        final Calendar dat = Calendar.getInstance();
        dat.set(Calendar.YEAR, year);
        dat.set(Calendar.MONTH, month);
        dat.set(Calendar.DAY_OF_MONTH, day);

        SimpleDateFormat format = new SimpleDateFormat("yyyy MMM dd");
        Toast.makeText(this, format.format(dat.getTime()), Toast.LENGTH_LONG).show();

        //}
    }


}

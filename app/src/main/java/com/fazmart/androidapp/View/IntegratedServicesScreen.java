package com.fazmart.androidapp.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.fazmart.androidapp.R;
import com.fazmart.androidapp.View.Checkout.DeliveryDataActivity;

public class IntegratedServicesScreen extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_screen);

        /*ViewFlipper viewFlipper = ((ViewFlipper) this.findViewById(R.id.flipper));
        viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
                R.anim.push_left_in));
        viewFlipper.startFlipping();*/

        ImageView kiranaWaalaa = (ImageView)findViewById(R.id.kirana_waalaa);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.landing_screen_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void GotoAptmtNbrhd (View view) {
        Toast.makeText(this, "Apartment Neighborhood", Toast.LENGTH_SHORT).show();
    }

    public void GotoGrocStore (View view) {
        Intent intent = new Intent(this, CategoryActivity.class);

        // Lineage is an array of all the parents that lie along a hierarchy branch
        // Root of the lineage is not a category. Hence, its Id is given a non-realistic number (-1).
        int[] lineageRoot = new int[1];
        lineageRoot[0] = -1;
        intent.putExtra(CategoryActivity.LINEAGE, lineageRoot);

        startActivity(intent);
    }

    public void GotoLogin (View view) {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
    }

    public void GotoDelInfo (View view) {
        Intent intent = new Intent(this, DeliveryDataActivity.class);
        startActivity(intent);
    }

    /*public void GotoPerfMeasurementUtility (View view) {
        Intent intent = new Intent(this, PerfActivity.class);
        startActivity(intent);
    }*/
}

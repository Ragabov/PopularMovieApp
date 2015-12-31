package com.ragab.ahmed.educational.movieapp.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.ragab.ahmed.educational.movieapp.R;
import com.ragab.ahmed.educational.movieapp.data.models.Movie;
import com.ragab.ahmed.educational.movieapp.ui.detailscreen.DetailFragment;
import com.ragab.ahmed.educational.movieapp.ui.mainscreen.MainFragment;
import com.ragab.ahmed.educational.movieapp.ui.utility.SettingsFragment;


public class MainActivity extends AppCompatActivity implements GridView.OnItemClickListener{

    private MainFragment mainFragment;
    private DetailFragment detailFragment;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = this.getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (mainFragment == null)
            mainFragment = new MainFragment();
        fragmentTransaction.add(R.id.main_fragment, mainFragment);
        fragmentTransaction.commit();

    }

    @Override
    public void onBackPressed() {
        if(fragmentManager.getBackStackEntryCount() != 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            fragmentManager = this.getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.main_fragment, new SettingsFragment());
            if (isLargeLayout(this))
            {
                if (detailFragment != null && detailFragment.isVisible())
                    fragmentTransaction.hide(detailFragment);
            }
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            
        }

        return super.onOptionsItemSelected(item);
    }

    public static boolean isLargeLayout(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        fragmentManager = this.getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        detailFragment = DetailFragment.newInstance(
                ((Movie)parent.getAdapter().getItem(position)).id
        );


        if (isLargeLayout(this))
        {
            fragmentTransaction.replace(R.id.detail_fragment, detailFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
        else
        {
            fragmentTransaction.replace(R.id.main_fragment, detailFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }
}

package com.homemade.tianp.rfidkeyboardwedge;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;


public class MainActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener, RFIDFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener,GestureDetector.OnGestureListener{

    private TextView mTextMessage;
    private GestureDetectorCompat gestureDetector;
    private Fragment selectedFragment = null;
    private ViewSwitcher viewSwitcher = null;
    public static BottomNavigationView navigation = null;
    public static Fragment homeFragment=null;
    public static Fragment rfidFragment=null;
    public static Fragment settingsFragment=null;
    private static String currentFragment = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupCreate();
        setupViews();
        setupStart();
    }



    private void setupCreate(){

    }
    private void setupViews(){
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        this.gestureDetector = new GestureDetectorCompat(this,this);

        mTextMessage = findViewById(R.id.mText);
    }

    private void setupStart(){
        navigation.setSelectedItemId(R.id.navigation_home);
    }




    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            selectedFragment = null;
            boolean fragmentAlreadyOpened = false;
            String FRAGMENT_TAG = "";
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    if(homeFragment != null){
                        selectedFragment = homeFragment;
                    }else{
                        selectedFragment = HomeFragment.newInstance();
                    }
                    FRAGMENT_TAG = "HomeFragment";
                    if (currentFragment == FRAGMENT_TAG)
                        fragmentAlreadyOpened = true;
                    break;
                case R.id.navigation_rfidsettings:
                    if(rfidFragment != null){
                        selectedFragment = rfidFragment;
                    }else {
                        selectedFragment = RFIDFragment.newInstance();
                    }
                    FRAGMENT_TAG = "RFIDFragment";
                    if (currentFragment == FRAGMENT_TAG)
                        fragmentAlreadyOpened = true;
                    break;
                case R.id.navigation_settings:
                    if(settingsFragment != null){
                        selectedFragment = settingsFragment;
                    }else {
                        selectedFragment = SettingsFragment.newInstance();
                        //selectedFragment = new SettingsFragment1.GeneralPreferenceFragment();
                    }
                    FRAGMENT_TAG = "SettingsFragment";
                    if (currentFragment == FRAGMENT_TAG)
                        fragmentAlreadyOpened = true;
                    break;
            }
            if(!fragmentAlreadyOpened){
                setCurrentFragment(FRAGMENT_TAG);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_layout, selectedFragment, FRAGMENT_TAG);
                transaction.commit();
            }

            return true;
        }
    };

    private void navigateToFragment(int id, String FRAGMENT_TAG){

        int page = 0;
        if(id == R.id.navigation_home){
            page = 0;
        }else if(id == R.id.navigation_rfidsettings){
            page = 1;
        }else if(id == R.id.navigation_settings){
            page = 2;
        }
        mOnNavigationItemSelectedListener.onNavigationItemSelected((MenuItem) navigation.getMenu().getItem(page));
        setCurrentFragment(FRAGMENT_TAG);
        navigation.setSelectedItemId(id);
    }

    /**Takes user to the settings where they can then choose to enable KBW Keyboard
     */
    public void openSettings(View view){
        startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
    }
    public void openRFIDKeyboardApp(View view){
        startActivity(new Intent(this, com.homemade.tianp.rfidkeyboardwedge.MainActivity.class));
    }
    public void openThingMagic(View view){
        startActivity(new Intent(this, com.thingmagic.rfidreader.ReaderActivity.class));
    }

    /**setDefaultKeyboard:
     * Takes user to the settings where they then have to set up the default keyboard
     */
    public void setDefaultKeyboard(View view){
        //startActivity(new Intent(Configs.ACTION_SETTINGS));

        Intent intent = new Intent();
        intent.setComponent( new ComponentName("com.android.settings","com.android.settings.Configs$InputMethodAndLanguageSettingsActivity" ));

        startActivity(intent);
    }

    public static String getCurrentFragment(){
        return currentFragment;
    }

    public static void setCurrentFragment(String string){
        currentFragment = string;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        //navigateBar(determineDirection(velocityX, velocityY));
        return false;
    }

    @Override
    public void onBackPressed() {
        if (currentFragment=="HomeFragment"){
            this.finish();
        } else if(currentFragment=="RFIDFragment") {
            navigateToFragment(R.id.navigation_home,"HomeFragment");
        } else if(currentFragment=="SettingsFragment") {
            navigateToFragment(R.id.navigation_home,"HomeFragment");
        } else if(currentFragment=="GeneralPreferenceFragment") {
            navigateToFragment(R.id.navigation_settings,"SettingsFragment");
        } else if(currentFragment=="ReadWritePreferenceFragment") {
            navigateToFragment(R.id.navigation_settings,"SettingsFragment");
        } else if(currentFragment=="PerformanceTuningPreferenceFragment") {
            navigateToFragment(R.id.navigation_settings,"SettingsFragment");
        } else if(currentFragment=="RegulatoryTestingPreferenceFragment") {
            navigateToFragment(R.id.navigation_settings,"SettingsFragment");
        } else if(currentFragment=="ProfilePreferenceFragment") {
            navigateToFragment(R.id.navigation_settings,"SettingsFragment");
        } else if(currentFragment=="FirmwarePreferenceFragment") {
            navigateToFragment(R.id.navigation_settings,"SettingsFragment");
        } else if(currentFragment=="AboutPreferenceFragment") {
            navigateToFragment(R.id.navigation_settings,"SettingsFragment");
        }


    }

    private int determineDirection(float velocityX, float velocityY){
        int direction = -1;
        if(velocityX < 0 && Math.abs(velocityX) > Math.abs(velocityY) ){
            //Move Right
            direction = 1;

        }else if(velocityX > 0 && Math.abs(velocityX) > Math.abs(velocityY) ){
            //Move Left
            direction = 0;
        }
        return direction;

    }

    private void navigateBar(int direction){
        if(selectedFragment.getTag() == "HomeFragment"){
            if(direction == 1){
                navigation.setSelectedItemId(R.id.navigation_rfidsettings);
            }else if(direction == 0){
            }
        }else if(selectedFragment.getTag() == "RFIDFragment"){
            if(direction == 1){
                navigation.setSelectedItemId(R.id.navigation_settings);
            }else if(direction == 0){
                navigation.setSelectedItemId(R.id.navigation_home);
            }
        }else if(selectedFragment.getTag() == "SettingsFragment"){
            if(direction == 1){

            }else if(direction == 0){
                navigation.setSelectedItemId(R.id.navigation_rfidsettings);
            }
        }
    }

    public void openCrossborder(View view) {
        String packageName = getString(R.string.CrossBorderApp_PackageName);
        Intent intent = getApplication().getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            // Bring user to the market or let them choose an app?
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + packageName));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplication().startActivity(intent);

    }

    public void openTFR(View view) {
        String packageName = getString(R.string.TransnetApp_PackageName);
        Intent intent = getApplication().getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            // Bring user to the market or let them choose an app?
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + packageName));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplication().startActivity(intent);

    }
}

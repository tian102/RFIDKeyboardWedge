/**
 * Manage app and reader settings
 */

package com.homemade.tianp.rfidkeyboardwedge;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.homemade.tianp.rfidkeyboardwedge.tools.FragmentMethods;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class SettingsFragment extends Fragment{

    private LinearLayout generalSettings = null;
    private LinearLayout rfidSettings = null;

    private LinearLayout settingsLayout = null;
    private LinearLayout readwriteSettings = null;
    private LinearLayout performanceTuningSettings = null;
    private LinearLayout displayOptionsSettings = null;
    private LinearLayout profileSettings = null;
    private LinearLayout firmwareSettings = null;
    private LinearLayout aboutSettings = null;

    private OnFragmentInteractionListener mListener;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupCreate();
        setupViews();
        setupStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {

            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void setupCreate(){
        readwriteSettings = getActivity().findViewById(R.id.settingsReadWrite);
        performanceTuningSettings = getActivity().findViewById(R.id.settingsPerformanceTuning);
        displayOptionsSettings = getActivity().findViewById(R.id.settingsDisplayOptions);
        profileSettings = getActivity().findViewById(R.id.settingsProfile);
        firmwareSettings = getActivity().findViewById(R.id.settingsFirmware);
        aboutSettings = getActivity().findViewById(R.id.settingsAbout);
    }

    private void setupViews(){

        readwriteSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RFIDKeyboardWedge.isReaderActive()){
                    Toast.makeText(getActivity(), "Cannot change settings while connected to reader", Toast.LENGTH_SHORT).show();
                }else {
                    openSettingsFragment(new FragmentMethods.ReadWritePreferenceFragment(), "ReadWritePreferenceFragment");
                }
            }
        });
        performanceTuningSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RFIDKeyboardWedge.isReaderActive()){
                    Toast.makeText(getActivity(), "Cannot change settings while connected to reader", Toast.LENGTH_SHORT).show();
                }else {
                    openSettingsFragment(new FragmentMethods.PerformanceTuningPreferenceFragment(), "PerformanceTuningPreferenceFragment");
                }
            }
        });
        displayOptionsSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RFIDKeyboardWedge.isReaderActive()){
                    Toast.makeText(getActivity(), "Cannot change settings while connected to reader", Toast.LENGTH_SHORT).show();
                }else {
                    openSettingsFragment(new FragmentMethods.DisplayOptionsPreferenceFragment(), "RegulatoryTestingPreferenceFragment");
                }
            }
        });
        profileSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RFIDKeyboardWedge.isReaderActive()){
                    Toast.makeText(getActivity(), "Cannot change settings while connected to reader", Toast.LENGTH_SHORT).show();
                }else {
                    openSettingsFragment(new FragmentMethods.ProfilePreferenceFragment(), "ProfilePreferenceFragment");
                }
            }
        });
        firmwareSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RFIDKeyboardWedge.isReaderActive()){
                    Toast.makeText(getActivity(), "Cannot change settings while connected to reader", Toast.LENGTH_SHORT).show();
                }else {
                    openSettingsFragment(new FragmentMethods.FirmwarePreferenceFragment(), "FirmwarePreferenceFragment");
                }
            }
        });
        aboutSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String FRAGMENT_TAG = "AboutPreferenceFragment";
                Fragment fragment = new FragmentMethods.AboutPreferenceFragment();
                MainActivity.setCurrentFragment(FRAGMENT_TAG);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_layout, fragment, FRAGMENT_TAG);
                transaction.commit();
            }
        });

    }

    private void openSettingsFragment(Fragment fragment, String Fragment_TAG){
        MainActivity.setCurrentFragment(Fragment_TAG);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, fragment, Fragment_TAG);
        transaction.commit();
    }

    private void setupStart(){

    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }


}

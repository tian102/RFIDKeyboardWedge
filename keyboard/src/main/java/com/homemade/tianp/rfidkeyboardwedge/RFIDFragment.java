/**
 * Fragment class used for RFID section of application UI
 *
 * @author  Tian Pretorius
 * @version 1.0
 * @since   2017-03-15
 *
 * Created by tianp on 24 Mar 2017.
 */

package com.homemade.tianp.rfidkeyboardwedge;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.homemade.tianp.rfidkeyboardwedge.tools.BTConnectionKB;
import com.homemade.tianp.rfidkeyboardwedge.tools.FragmentMethods;
import com.thingmagic.Reader;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RFIDFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RFIDFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RFIDFragment extends Fragment {
    public Reader reader = null;

    private OnFragmentInteractionListener mListener;
    private RFIDFragment mRfidFragment = null;
        // private Spinner spinner1;
        // private Button buttonConnectBT;


    public RFIDFragment() {
        // Required empty public constructor
    }

    public static RFIDFragment newInstance() {
        return new RFIDFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_rfid, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setupCreate();
        setupViews();
        setupStart();

        super.onViewCreated(view, savedInstanceState);
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
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //
    //                                      Methods
    //
    ////////////////////////////////////////////////////////////////////////////////////////////


    private void setupCreate() {
        this.mRfidFragment = this;

    }


    private void setupViews() {


    }


    private void setupStart() {


    }

    private void loadHTML(WebView webView){
        webView.setWebChromeClient(new WebChromeClient());

        webView.getSettings().setJavaScriptEnabled(true);


        webView.loadUrl("https://docs.google.com/forms/d/e/1FAIpQLSf88r2J1X_7d29QN3S5pXUQFJcqIi590125a7JHWYrX6JU6gQ/viewform?usp=sf_link");
    }

}

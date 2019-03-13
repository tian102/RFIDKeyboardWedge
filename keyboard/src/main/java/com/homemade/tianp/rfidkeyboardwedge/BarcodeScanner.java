
package com.homemade.tianp.rfidkeyboardwedge;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.homemade.tianp.rfidkeyboardwedge.R;
import com.homemade.tianp.rfidkeyboardwedge.ScanResult;

/**Handles the scanning of 1D and 2D barcodes.
 *
 * @author  Tian Pretorius
 * @version 1.0
 * @since   2017-03-15
 *
 * Created by tianp on 24 Mar 2017.
 */

public class BarcodeScanner extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);
        StartBarcodeScanner();
    }

    /**Set up the scanner
     * */
    public void StartBarcodeScanner(){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Place a barcode inside the viewfinder rectangle to scan it.");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();

    }

    /**Handles result of barcode scan
     * @param requestCode
     * @param resultCode
     * @param data
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==IntentIntegrator.REQUEST_CODE)
        {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            if(scanResult.getContents()==null){
                Toast.makeText(this, R.string.Scan_Cancelled, Toast.LENGTH_SHORT).show();
                finish();

            }
            else if (scanResult != null)
            {
                // Handle scan result
                ScanResult.SetProductID(scanResult.getContents());
                ScanResult.SetHistoryProductID(scanResult.getContents());
                Toast.makeText(this, "Tag scanned:\n" + scanResult.getContents(), Toast.LENGTH_LONG).show();
                Toast.makeText(this, "Press 'PRINT' to add", Toast.LENGTH_SHORT).show();
                finish();
            }
            else
            {
                // Else continue with any other code you need in the method
                Toast.makeText(this, R.string.Scan_Null, Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }



}


package com.homemade.tianp.rfidkeyboardwedge;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

/**Handles the scanning of 1D and 2D barcodes.
 *
 * @author  Tian Pretorius
 * @version 1.0
 * @since   2017-03-15
 *
 * Created by tianp on 24 Mar 2017.
 */

public class ContinuousCaptureActivity extends Activity {

    private static final String TAG = ContinuousCaptureActivity.class.getSimpleName();
    private DecoratedBarcodeView barcodeView;
    private BeepManager beepManager;
    private String lastText;

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if(result.getText() == null){
                Toast.makeText(ContinuousCaptureActivity.this, R.string.Scan_Cancelled, Toast.LENGTH_SHORT).show();
                finish();
            }

            if(result.getText().equals(lastText)) {

                return;
            }

            lastText = result.getText();

            if(lastText!=null || !lastText.isEmpty()){
                ScanResult.SetProductID_Array(lastText);
                ScanResult.SetHistoryProductID_Array(lastText);
            }
            ScanResult.SetProductID_Array(lastText);
            ScanResult.SetHistoryProductID_Array(lastText);
            barcodeView.setStatusText(result.getText());
            beepManager.playBeepSoundAndVibrate();


            /*//Added preview of scanned barcode
            ImageView imageView = (ImageView) findViewById(R.id.barcodePreview);
            imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));*/
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.continuous_scan);

        barcodeView = (DecoratedBarcodeView) findViewById(R.id.barcode_scanner);
        barcodeView.decodeContinuous(callback);

        beepManager = new BeepManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        barcodeView.pause();
    }

    public void pause(View view) {
        barcodeView.pause();
    }

    public void resume(View view) {
        barcodeView.resume();
    }


    public void triggerScan(View view) {
        barcodeView.decodeSingle(callback);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        ScanResult.convertProductID_Array();
        ScanResult.convertHistoryProductID_Array();

        Toast.makeText(getApplication(), "Tags scanned:\n" + ScanResult.GetProductID(), Toast.LENGTH_LONG).show();
        Toast.makeText(getApplication(), "Press 'PRINT' to add", Toast.LENGTH_SHORT).show();
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}
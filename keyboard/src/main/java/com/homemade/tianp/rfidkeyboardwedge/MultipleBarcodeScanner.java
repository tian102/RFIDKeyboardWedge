
package com.homemade.tianp.rfidkeyboardwedge;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


/**Handles the scanning of 1D and 2D barcodes.
 *
 * @author  Tian Pretorius
 * @version 1.0
 * @since   2017-03-15
 *
 * Created by tianp on 24 Mar 2017.
 */

public class MultipleBarcodeScanner extends AppCompatActivity {

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
        integrator.setPrompt("Scan");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
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

                /*String barcodeImagePath = scanResult.getBarcodeImagePath();
                //Toast Image Result
//                LayoutInflater inflater = getLayoutInflater();
//                View view = inflater.inflate(R.layout.cust_toast_layout,
//                        (ViewGroup) findViewById(R.id.relativeLayout1));
//
//                ImageView imageView = (ImageView) view.findViewById(R.id.imageView1);
//                imageView.setImageBitmap(BitmapFactory.decodeFile(scanResult.getBarcodeImagePath()));
//                Toast toast = new Toast(this);
//                toast.setView(view);
//                toast.show();
                Bitmap inputBitmap = BitmapFactory.decodeFile(scanResult.getBarcodeImagePath());
*/

                ScanResult.SetProductID(scanResult.getContents());
                ScanResult.SetHistoryProductID(scanResult.getContents());

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

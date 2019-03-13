package com.homemade.tianp.rfidkeyboardwedge;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.homemade.tianp.rfidkeyboardwedge.R;
import com.homemade.tianp.rfidkeyboardwedge.ScanResult;
import com.nxp.nfclib.CardType;
import com.nxp.nfclib.NxpNfcLib;
import com.nxp.nfclib.classic.ClassicFactory;
import com.nxp.nfclib.classic.IMFClassic;
import com.nxp.nfclib.classic.IMFClassicEV1;
import com.nxp.nfclib.desfire.DESFireFactory;
import com.nxp.nfclib.desfire.IDESFireEV1;
import com.nxp.nfclib.desfire.IDESFireEV2;
import com.nxp.nfclib.exceptions.NxpNfcLibException;
import com.nxp.nfclib.icode.ICodeFactory;
import com.nxp.nfclib.icode.IICodeDNA;
import com.nxp.nfclib.icode.IICodeSLI;
import com.nxp.nfclib.icode.IICodeSLIL;
import com.nxp.nfclib.icode.IICodeSLIS;
import com.nxp.nfclib.icode.IICodeSLIX;
import com.nxp.nfclib.icode.IICodeSLIX2;
import com.nxp.nfclib.icode.IICodeSLIXL;
import com.nxp.nfclib.icode.IICodeSLIXS;
import com.nxp.nfclib.ntag.INTAGI2Cplus;
import com.nxp.nfclib.ntag.INTag;
import com.nxp.nfclib.ntag.INTag203x;
import com.nxp.nfclib.ntag.INTag210;
import com.nxp.nfclib.ntag.INTag210u;
import com.nxp.nfclib.ntag.INTag213215216;
import com.nxp.nfclib.ntag.INTag213F216F;
import com.nxp.nfclib.ntag.INTagI2C;
import com.nxp.nfclib.ntag.NTagFactory;
import com.nxp.nfclib.plus.IPlusEV1SL0;
import com.nxp.nfclib.plus.IPlusEV1SL1;
import com.nxp.nfclib.plus.IPlusEV1SL3;
import com.nxp.nfclib.plus.IPlusSL0;
import com.nxp.nfclib.plus.IPlusSL1;
import com.nxp.nfclib.plus.IPlusSL3;
import com.nxp.nfclib.plus.PlusFactory;
import com.nxp.nfclib.plus.PlusSL1Factory;
import com.nxp.nfclib.ultralight.IUltralight;
import com.nxp.nfclib.ultralight.IUltralightC;
import com.nxp.nfclib.ultralight.IUltralightEV1;
import com.nxp.nfclib.ultralight.IUltralightNano;
import com.nxp.nfclib.ultralight.UltralightFactory;
import com.nxp.nfclib.utils.Utilities;

/**Handles the scanning of (compatible) NFC and HF RFID tags.
 *
 * @author  Tian Pretorius
 * @version 1.0
 * @since   2017-03-15
 *
 *
 * Created by tianp on 24 Mar 2017.
 */

public class NFCTagScanner extends AppCompatActivity {

    private NxpNfcLib m_libInstance = null;
    private IDESFireEV1 desFireEV1;
    private IDESFireEV2 desFireEV2;
    private IMFClassic mifareClassic;
    private IMFClassicEV1 mifareClassicEv1 = null;
    private IUltralight ultralightBase;
    private IUltralightC ultralightC;
    private IUltralightEV1 ultralightEV1;
    private INTag203x nTAG203x;
    private INTag210 nTAG210;
    private INTag210u nTAG210u;
    private INTagI2C nTAGI2C;
    private INTAGI2Cplus nTAGI2CPlus;
    private INTag213215216 nTAG213215216;
    private INTag213F216F nTAG213F216F;
    private IICodeSLI icodeSLI;
    private IICodeSLIS icodeSLIS;
    private IICodeSLIL icodeSLIL;
    private IICodeSLIX icodeSLIX;
    private IICodeSLIXS icodeSLIXS;
    private IICodeSLIXL icodeSLIXL;
    private IICodeSLIX2 icodeSLIX2;
    private IICodeDNA icodeDNA;
    private IUltralightNano ultralightNano;
    private IPlusSL0 plusSL0 = null;
    private IPlusSL1 plusSL1 = null;
    private IPlusEV1SL3 plusEV1SL3;
    private IPlusEV1SL0 plusEV1SL0;
    private IPlusEV1SL1 plusEV1SL1;
    private IPlusSL3 plusSL3 = null;
    private static final int DEFAULT_SECTOR_CLASSIC = 6;
    private CardType mCardType = CardType.UnknownCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_scanner);

        // Initialize NXP/TapLinx library
        initializeLibrary();
    }

    @Override
    protected void onResume(){                              // Called if app becomes active
        m_libInstance.startForeGroundDispatch();
        super.onResume();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onPause() {
        m_libInstance.stopForeGroundDispatch();
        super.onPause();
    }

    /**Handles new scan result
     * @param intent Intent is used to get context of the scan intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        cardLogic(intent);
        finish();
        super.onNewIntent(intent);
    }

    /**Start instance of NXP library
     */
    private void initializeLibrary() {
        m_libInstance = NxpNfcLib.getInstance();
        //Register Activity at https://inspire.nxp.com/mifare/myapp.html
        m_libInstance.registerActivity(this, "5869f2737d3d5a067f3464117b7eab2d");

    }

    /**Determine the type of tag that has been scanned, as well as the UID
     * @param intent Intent is used to get context of the scan intent
     */
    private void cardLogic(final Intent intent) {
        CardType type = CardType.UnknownCard;
        try {
            type = m_libInstance.getCardType(intent);
        } catch (NxpNfcLibException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        switch (type) {

            case MIFAREClassic: {
                if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
                    Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                    if (tag != null) {
                        mifareClassic = ClassicFactory.getInstance().getClassic(MifareClassic.get(tag));
                        mCardType = CardType.MIFAREClassic;
                        classicCardLogic();
                    }
                }
                break;
            }
            case MIFAREClassicEV1: {
                if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
                    Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                    if (tag != null) {
                        mifareClassicEv1 = ClassicFactory.getInstance().getClassicEV1(MifareClassic.get(tag));
                        mCardType = CardType.MIFAREClassicEV1;
                        classicCardEV1Logic();
                    }
                }
                break;
            }
            case Ultralight:
                ultralightBase = UltralightFactory.getInstance().getUltralight(m_libInstance.getCustomModules());
                mCardType = CardType.Ultralight;
                try {
                    ultralightBase.getReader().connect();
                    ultralightCardLogic();
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case UltralightEV1_11:
                mCardType = CardType.UltralightEV1_11;
                ultralightEV1 = UltralightFactory.getInstance().getUltralightEV1(m_libInstance.getCustomModules());
                try {
                    ultralightEV1.getReader().connect();
                    ultralightEV1CardLogic();
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case UltralightEV1_21:
                mCardType = CardType.UltralightEV1_21;
                ultralightEV1 = UltralightFactory.getInstance().getUltralightEV1(m_libInstance.getCustomModules());
                try {
                    ultralightEV1.getReader().connect();
                    ultralightEV1CardLogic();
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case UltralightC:
                mCardType = CardType.UltralightC;
                ultralightC = UltralightFactory.getInstance().getUltralightC(m_libInstance.getCustomModules());
                try {
                    ultralightC.getReader().connect();
                    ultralightcCardLogic();
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case NTag203X:
                mCardType = CardType.NTag203X;
                nTAG203x = NTagFactory.getInstance().getNTAG203x(m_libInstance.getCustomModules());
                try {
                    nTAG203x.getReader().connect();
                    ntagCardLogic(nTAG203x);
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case NTag210:
                mCardType = CardType.NTag210;
                nTAG210 = NTagFactory.getInstance().getNTAG210(m_libInstance.getCustomModules());
                try {
                    nTAG210.getReader().connect();
                    ntagCardLogic(nTAG210);
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case NTag213:
                mCardType = CardType.NTag213;
                nTAG213215216 = NTagFactory.getInstance().getNTAG213(m_libInstance.getCustomModules());
                try {
                    nTAG213215216.getReader().connect();
                    ntagCardLogic(nTAG213215216);
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case NTag215:
                mCardType = CardType.NTag215;
                nTAG213215216 = NTagFactory.getInstance().getNTAG215(m_libInstance.getCustomModules());
                try {
                    nTAG213215216.getReader().connect();
                    ntagCardLogic(nTAG213215216);
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case NTag216:
                mCardType = CardType.NTag216;
                nTAG213215216 = NTagFactory.getInstance().getNTAG216(m_libInstance.getCustomModules());
                try {
                    nTAG213215216.getReader().connect();
                    ntagCardLogic(nTAG213215216);
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case NTag213F:
                mCardType = CardType.NTag213F;
                nTAG213F216F = NTagFactory.getInstance().getNTAG213F(m_libInstance.getCustomModules());

                try {
                    nTAG213F216F.getReader().connect();
                    ntagCardLogic(nTAG213F216F);
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case NTag216F:
                mCardType = CardType.NTag216F;
                nTAG213F216F = NTagFactory.getInstance().getNTAG216F(m_libInstance.getCustomModules());

                try {
                    nTAG213F216F.getReader().connect();
                    ntagCardLogic(nTAG213F216F);
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case NTagI2C1K:
                mCardType = CardType.NTagI2C1K;
                nTAGI2C = NTagFactory.getInstance().getNTAGI2C1K(m_libInstance.getCustomModules());
                try {
                    nTAGI2C.getReader().connect();
                    ntagCardLogic(nTAGI2C);
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case NTagI2C2K:
                mCardType = CardType.NTagI2C2K;
                nTAGI2C = NTagFactory.getInstance().getNTAGI2C2K(m_libInstance.getCustomModules());
                try {
                    nTAGI2C.getReader().connect();
                    ntagCardLogic(nTAGI2C);
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case NTagI2CPlus1K:
                mCardType = CardType.NTagI2CPlus1K;
                nTAGI2CPlus = NTagFactory.getInstance().getNTAGI2CPlus1K(m_libInstance.getCustomModules());
                try {
                    nTAGI2CPlus.getReader().connect();
                    ntagCardLogic(nTAGI2CPlus);
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case NTagI2CPlus2K:
                mCardType = CardType.NTagI2CPlus2K;
                nTAGI2CPlus = NTagFactory.getInstance().getNTAGI2CPlus2K(m_libInstance.getCustomModules());
                try {
                    nTAGI2CPlus.getReader().connect();
                    ntagCardLogic(nTAGI2CPlus);
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case NTag210u:
                mCardType = CardType.NTag210u;
                nTAG210u = NTagFactory.getInstance().getNTAG210u(m_libInstance.getCustomModules());
                try {
                    nTAG210u.getReader().connect();
                    ntagCardLogic(nTAG210u);
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case ICodeSLI:
                mCardType = CardType.ICodeSLI;
                icodeSLI = ICodeFactory.getInstance().getICodeSLI(m_libInstance.getCustomModules());
                try {
                    icodeSLI.getReader().connect();
                    iCodeSLICardLogic();
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case ICodeSLIS:
                mCardType = CardType.ICodeSLIS;
                icodeSLIS = ICodeFactory.getInstance().getICodeSLIS(m_libInstance.getCustomModules());
                try {
                    if (!icodeSLIS.getReader().isConnected())
                        icodeSLIS.getReader().connect();
                    iCodeSLISCardLogic();
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case ICodeSLIL:
                mCardType = CardType.ICodeSLIL;
                icodeSLIL = ICodeFactory.getInstance().getICodeSLIL(m_libInstance.getCustomModules());
                try {
                    icodeSLIL.getReader().connect();
                    iCodeSLILCardLogic();
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case ICodeSLIX:
                mCardType = CardType.ICodeSLIX;
                icodeSLIX = ICodeFactory.getInstance().getICodeSLIX(m_libInstance.getCustomModules());
                try {
                    icodeSLIX.getReader().connect();
                    iCodeSLIXCardLogic();
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case ICodeSLIXS:
                mCardType = CardType.ICodeSLIXS;
                icodeSLIXS = ICodeFactory.getInstance().getICodeSLIXS(m_libInstance.getCustomModules());
                try {
                    icodeSLIXS.getReader().connect();
                    iCodeSLIXSCardLogic();
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case ICodeSLIXL:
                mCardType = CardType.ICodeSLIXL;
                icodeSLIXL = ICodeFactory.getInstance().getICodeSLIXL(m_libInstance.getCustomModules());
                try {
                    icodeSLIXL.getReader().connect();
                    iCodeSLIXLCardLogic();
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case ICodeSLIX2:
                mCardType = CardType.ICodeSLIX2;
                icodeSLIX2 = ICodeFactory.getInstance().getICodeSLIX2(m_libInstance.getCustomModules());

                try {
                    icodeSLIX2.getReader().connect();
                    iCodeSLIX2CardLogic();
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case ICodeDNA:
                mCardType = CardType.ICodeDNA;
                icodeDNA = ICodeFactory.getInstance().getICodeDNA(m_libInstance.getCustomModules());

                try {
                    if (!icodeDNA.getReader().isConnected())
                        icodeDNA.getReader().connect();
                    iCodeDNACardLogic();
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case DESFireEV1:
                mCardType = CardType.DESFireEV1;
                desFireEV1 = DESFireFactory.getInstance().getDESFire(m_libInstance.getCustomModules());
                try {

                    desFireEV1.getReader().connect();
                    desFireEV1.getReader().setTimeout(2000);
                    desfireEV1CardLogic();

                } catch (Throwable t) {
                    t.printStackTrace();
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case DESFireEV2:
                mCardType = CardType.DESFireEV2;

                desFireEV2 = DESFireFactory.getInstance().getDESFireEV2(m_libInstance.getCustomModules());
                try {
                    desFireEV2.getReader().connect();
                    desfireEV2CardLogic();

                } catch (Throwable t) {
                    t.printStackTrace();
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case PlusSL0:
                mCardType = CardType.PlusSL0;

                plusSL0 = PlusFactory.getInstance().getPlusSL0(m_libInstance.getCustomModules());


                easyToast("No operations are executed on a Plus SL0 card");
                break;
            case PlusSL1:
                mCardType = CardType.PlusSL1;

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                MifareClassic obj = MifareClassic.get(tag);
                if (obj != null) {
                    plusSL1 = PlusSL1Factory.getInstance().getPlusSL1(m_libInstance.getCustomModules(), obj);
                    plusSL1CardLogic();
                } else {
                    plusSL1 = PlusSL1Factory.getInstance().getPlusSL1(m_libInstance.getCustomModules());

                    easyToast("Plus SL1 card's Classic compatible methods not available on this device!");
                }
                break;
            case PlusSL3:
                mCardType = CardType.PlusSL3;
                plusSL3 = PlusFactory.getInstance().getPlusSL3(m_libInstance.getCustomModules());

                try {
                    plusSL3.getReader().connect();
                    plusSL3CardLogic();

                } catch (Throwable t) {
                    t.printStackTrace();
                    easyToast("Unknown Error Tap Again!");
                }
                break;

            case PlusEV1SL0:
                mCardType = CardType.PlusSL0;

                plusEV1SL0 = PlusFactory.getInstance().getPlusEV1SL0(m_libInstance.getCustomModules());
                try {
                    plusEV1SL0.getReader().connect();

                    easyToast("Card Detected :" + plusEV1SL0.getType().getTagName());
                    easyToast("No operations are executed on a Plus EV1 SL0 card");
                } catch (Throwable t) {
                    t.printStackTrace();
                    easyToast("Unknown Error Tap Again!");
                }

                break;
            case PlusEV1SL1:
                mCardType = CardType.PlusEV1SL1;

                tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                obj = MifareClassic.get(tag);
                if (obj != null) {
                    plusEV1SL1 = PlusSL1Factory.getInstance().getPlusEV1SL1(m_libInstance.getCustomModules(), obj);
                    plusEV1SL1CardLogic();
                } else {
                    plusEV1SL1 = PlusSL1Factory.getInstance().getPlusEV1SL1(m_libInstance.getCustomModules());

                    easyToast("Card Detected : " + plusEV1SL1.getType().getTagName());
                    easyToast("Plus SL1 card's Classic compatible methods not available on this device!");
                }
                break;
            case PlusEV1SL3:
                mCardType = CardType.PlusEV1SL3;
                plusEV1SL3 = PlusFactory.getInstance().getPlusEV1SL3(m_libInstance.getCustomModules());

                try {
                    if (!plusEV1SL3.getReader().isConnected())
                        plusEV1SL3.getReader().connect();
                    plusEV1SL3CardLogic();
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
            case UltralightNano_40:
                mCardType = CardType.UltralightNano_40;
                ultralightNano = UltralightFactory.getInstance().getUltralightNano(m_libInstance.getCustomModules());

                try {
                    if (!ultralightNano.getReader().isConnected())
                        ultralightNano.getReader().connect();
                    ultralightNanoCardLogic();
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;

            case UltralightNano_48:
                mCardType = CardType.UltralightNano_48;
                ultralightNano = UltralightFactory.getInstance().getUltralightNano(m_libInstance.getCustomModules());
                try {
                    if (!ultralightNano.getReader().isConnected())
                        ultralightNano.getReader().connect();
                    ultralightNanoCardLogic();
                } catch (Throwable t) {
                    easyToast("Unknown Error Tap Again!");
                }
                break;
        }
    }

    protected void classicCardLogic() {

        easyToast("Card Detected : " + mifareClassic.getType().getTagName());
        try {
            //Call connect first is the Reader is not connected
            if (!mifareClassic.getReader().isConnected()) {
                mifareClassic.getReader().connect();
            }
            ScanResult.SetProductID(Utilities.dumpBytes(mifareClassic.getUID()));
            ScanResult.SetHistoryProductID(Utilities.dumpBytes(mifareClassic.getUID()));

        } catch (Exception e) {
            easyToast("Exception occurred... check LogCat");
            e.printStackTrace();
        }
    }

    protected void classicCardEV1Logic() {

        easyToast("Card Detected : " + mifareClassicEv1.getType().getTagName());

        try {
            //Call connect first is the Reader is not connected
            if (!mifareClassicEv1.getReader().isConnected()) {
                mifareClassicEv1.getReader().connect();
            }
            ScanResult.SetProductID(Utilities.dumpBytes(mifareClassicEv1.getUID()));
            ScanResult.SetHistoryProductID(Utilities.dumpBytes(mifareClassicEv1.getUID()));

        } catch (Exception e) {
            easyToast("Exception occurred... check LogCat");
            e.printStackTrace();
        }
    }

    private void ultralightCardLogic() {

        easyToast("Card Detected : " + ultralightBase.getType().getTagName());

        try {
            ScanResult.SetProductID(Utilities.dumpBytes(ultralightBase.getUID()));
            ScanResult.SetHistoryProductID(Utilities.dumpBytes(ultralightBase.getUID()));

        } catch (Exception e) {
            easyToast(e.getMessage());
            easyToast("IOException occurred... check LogCat");
            e.printStackTrace();
        }
    }
    private void ultralightEV1CardLogic() {

        easyToast("Card Detected : " + ultralightEV1.getType().getTagName());

        try {
            ScanResult.SetProductID(Utilities.dumpBytes(ultralightEV1.getUID()));
            ScanResult.SetHistoryProductID(Utilities.dumpBytes(ultralightEV1.getUID()));

        } catch (Exception e) {
            easyToast(e.getMessage());
            easyToast("Exception occurred... check LogCat");
            e.printStackTrace();
        }
    }

    private void ultralightcCardLogic() {
        easyToast("Card Detected : " + ultralightC.getType().getTagName());

        try {
            ScanResult.SetProductID(Utilities.dumpBytes(ultralightC.getUID()));
            ScanResult.SetHistoryProductID(Utilities.dumpBytes(ultralightC.getUID()));

        } catch (Exception e) {
            e.printStackTrace();
            easyToast(e.getMessage());
            easyToast("IOException occurred... check LogCat");

        }
    }

    private void ntagCardLogic(final INTag tag) {

        easyToast("Card Detected : " + tag.getType().getTagName());

        try {
            ScanResult.SetProductID(Utilities.dumpBytes(tag.getUID()));
            ScanResult.SetHistoryProductID(Utilities.dumpBytes(tag.getUID()));
            tag.getReader().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void iCodeSLICardLogic() {

        easyToast("Card Detected : " + icodeSLI.getType().getTagName());
        try {
            ScanResult.SetProductID(Utilities.dumpBytes(icodeSLI.getUID()));
            ScanResult.SetHistoryProductID(Utilities.dumpBytes(icodeSLI.getUID()));

        } catch (Exception e) {
            easyToast("IO Exception -  Check logcat!");
            e.printStackTrace();
        }

    }
    private void iCodeSLISCardLogic() {
        byte[] out = null;


        easyToast("Card Detected : " + icodeSLIS.getType().getTagName());

        try {
            ScanResult.SetProductID(Utilities.dumpBytes(icodeSLIS.getUID()));
            ScanResult.SetHistoryProductID(Utilities.dumpBytes(icodeSLIS.getUID()));
        } catch (Exception e) {
            easyToast("Exception -  Check logcat!");
            e.printStackTrace();
        }
    }

    private void iCodeSLILCardLogic() {

        easyToast("Card Detected : " + icodeSLIL.getType().getTagName());
        try {
            ScanResult.SetProductID(Utilities.dumpBytes(icodeSLIL.getUID()));
            ScanResult.SetHistoryProductID(Utilities.dumpBytes(icodeSLIL.getUID()));

        } catch (Exception e) {
            easyToast("IO Exception -  Check logcat!");
            e.printStackTrace();
        }
    }

    private void iCodeSLIXCardLogic() {
        easyToast("Card Detected : " + icodeSLIX.getType().getTagName());
        try {
            ScanResult.SetProductID(Utilities.dumpBytes(icodeSLIX.getUID()));
            ScanResult.SetHistoryProductID(Utilities.dumpBytes(icodeSLIX.getUID()));

        } catch (Exception e) {
            easyToast("IO Exception -  Check logcat!");
            e.printStackTrace();
        }

    }

    private void iCodeSLIXSCardLogic() {

        easyToast("Card Detected : " + icodeSLIXS.getType().getTagName());
        try {
            ScanResult.SetProductID(Utilities.dumpBytes(icodeSLIXS.getUID()));
            ScanResult.SetHistoryProductID(Utilities.dumpBytes(icodeSLIXS.getUID()));

        } catch (Exception e) {
            easyToast("IO Exception -  Check logcat!");
            e.printStackTrace();
        }
    }

    private void iCodeSLIXLCardLogic() {

        easyToast("Card Detected : " + icodeSLIXL.getType().getTagName());
        try {
            ScanResult.SetProductID(Utilities.dumpBytes(icodeSLIXL.getUID()));
            ScanResult.SetHistoryProductID(Utilities.dumpBytes(icodeSLIXL.getUID()));

        } catch (Exception e) {
            easyToast("IO Exception -  Check logcat!");
            e.printStackTrace();
        }
    }
    private void iCodeSLIX2CardLogic() {

        easyToast("Card Detected : " + icodeSLIX2.getType().getTagName());
        try {
            ScanResult.SetProductID(Utilities.dumpBytes(icodeSLIX2.getUID()));
            ScanResult.SetHistoryProductID(Utilities.dumpBytes(icodeSLIX2.getUID()));

        } catch (Exception e) {
            easyToast("IO Exception -  Check logcat!");
            e.printStackTrace();
        }
    }

    private void iCodeDNACardLogic() {

        easyToast("Card Detected : " + icodeDNA.getType().getTagName());
        try {
            ScanResult.SetProductID(Utilities.dumpBytes(icodeDNA.getUID()));
            ScanResult.SetHistoryProductID(Utilities.dumpBytes(icodeDNA.getUID()));

        } catch (Exception e) {
            easyToast("SmartCard Exception - Check logcat!");
            e.printStackTrace();
        }
    }

    private void desfireEV1CardLogic() {
        int timeOut = 2000;

        easyToast("Card Detected : " + desFireEV1.getType().getTagName());

        try {
            desFireEV1.getReader().setTimeout(timeOut);
            // Must be tested
            // easyToast("Existing Applications Ids : " + Arrays.toString(desFireEV1.getApplicationIDs()));

            ScanResult.SetProductID(Utilities.dumpBytes(desFireEV1.getUID()));
            ScanResult.SetHistoryProductID(Utilities.dumpBytes(desFireEV1.getUID()));
            desFireEV1.getReader().close();

        } catch (Exception e) {
            easyToast("IOException occurred... check LogCat");
            e.printStackTrace();
        }

    }

    private void ultralightNanoCardLogic() {

        easyToast("Card Detected : " + ultralightNano.getType().getTagName());

        try {
            ScanResult.SetProductID(Utilities.dumpBytes(ultralightNano.getUID()));
            ScanResult.SetHistoryProductID(Utilities.dumpBytes(ultralightNano.getUID()));

        } catch (Exception e) {
            easyToast(e.getMessage());
            easyToast("IOException occurred... check LogCat");
            e.printStackTrace();
        }
    }

    private void plusEV1SL3CardLogic() {

        easyToast("Card Detected : " + plusEV1SL3.getType().getTagName());
        try {
            ScanResult.SetProductID(Utilities.dumpBytes(plusEV1SL3.getUID()));
            ScanResult.SetHistoryProductID(Utilities.dumpBytes(plusEV1SL3.getUID()));
        } catch (Exception e) {
            easyToast(e.getMessage());
            easyToast("Exception occurred... check LogCat");
            e.printStackTrace();
        }
    }

    private void plusEV1SL1CardLogic() {

        try {
            easyToast("Card Detected :" + plusEV1SL1.getType().getTagName());
            plusEV1SL1.getReader().setTimeout(1000);
            if (!plusEV1SL1.getReader().isConnected()) {
                plusEV1SL1.getReader().connect();
            }

            plusEV1SL1.activateLayer4();
            plusEV1SL1.getSL3SectorHelper().authenticateFirst(0x4004, null, null);
            easyToast("SL3 sector authenticated :" + plusEV1SL1.getType().getTagName());
            ScanResult.SetProductID(Utilities.dumpBytes(plusEV1SL1.getUID()));
            ScanResult.SetHistoryProductID(Utilities.dumpBytes(plusEV1SL1.getUID()));

        } catch (Exception e) {
            easyToast("Exception occurred... check LogCat");
            e.printStackTrace();
        }
    }

    private void plusSL3CardLogic() {

        if (plusSL3.getCardDetails().securityLevel.equals("Security Level 3")) {
            try {

                /** ALL WORK RELATED TO MIFARE PLUS SL3 card. */
                // Must be tested
                plusSL3.authenticateFirst(0x4004, null, null);
                ScanResult.SetProductID(Utilities.dumpBytes(plusSL3.getUID()));
                ScanResult.SetHistoryProductID(Utilities.dumpBytes(plusSL3.getUID()));

            } catch (Exception e) {
                easyToast(e.getMessage());
                easyToast("Exception occurred... check LogCat");
                e.printStackTrace();
            }
        } else {
            easyToast("No operations done since card is in security level 0");
        }
    }

    private void plusSL1CardLogic() {
        easyToast("Card Detected : " + plusSL1.getType().getTagName());
        // ******* Note that all the classic APIs work well with Plus Security
        // Level 1 *******
        int blockTorw = DEFAULT_SECTOR_CLASSIC;
        int sectorOfBlock = 0;

        if (!plusSL1.getReader().isConnected())
            plusSL1.getReader().connect();

        try {
            sectorOfBlock = plusSL1.blockToSector(blockTorw);
            plusSL1.authenticateSectorWithKeyA(sectorOfBlock, null);
            ScanResult.SetProductID(Utilities.dumpBytes(plusSL1.getUID()));
            ScanResult.SetHistoryProductID(Utilities.dumpBytes(plusSL1.getUID()));

        } catch (Exception e) {
            easyToast("Exception occurred... check LogCat");
            e.printStackTrace();
        }
    }

    private void desfireEV2CardLogic() {
        int timeOut = 2000;

        easyToast("Card Detected : " + desFireEV2.getType().getTagName());

        try {
            desFireEV2.getReader().setTimeout(timeOut);
            // Must be tested
            // easyToast("Existing Applications Ids : " + Arrays.toString(desFireEV2.getApplicationIDs()));
            ScanResult.SetProductID(Utilities.dumpBytes(plusSL1.getUID()));
            ScanResult.SetHistoryProductID(Utilities.dumpBytes(plusSL1.getUID()));

        } catch (Exception e) {
            easyToast("IOException occurred... check LogCat");
            e.printStackTrace();
        }
    }

    // Easier to type "easyToast(string)" than it is to type "Toast.makeText(this, string, Toast.LENGTH_SHORT).show()" ;)
    private void easyToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

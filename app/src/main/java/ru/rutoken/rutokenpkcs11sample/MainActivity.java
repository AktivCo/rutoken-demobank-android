package ru.rutoken.rutokenpkcs11sample;

/*
 * @author Aktiv Co. <hotline@rutoken.ru>
 */

import ru.rutoken.Pkcs11.CK_TOKEN_INFO;
import ru.rutoken.Pkcs11.Pkcs11;
import ru.rutoken.Pkcs11.Pkcs11Constants;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.app.Activity;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.rutoken.Pkcs11Caller.RtPkcs11Library;

class Updatable {
    public boolean isUpdated = true;
}

class Token extends Updatable {
    public int hSession = -1;
    public int slotid = -1;
    public CK_TOKEN_INFO info;
    Token(int slotid, CK_TOKEN_INFO info) {
        this.slotid = slotid;
        this.info = info;
    }
}

class ByteArrayConverter {
    public static String byteArrayToString(byte[] array) {
        String s = new String("");
        for (int i = 0; i < array.length; ++i)
            s+=(char)array[i];
        return s;
    }
}

class InternalException extends Exception {
    InternalException(String text) {
        super(text);
    }
}


public class MainActivity extends Activity {
    // GUI
    TextView debugPrint;
    Spinner tokenSpinner;
    Button updateListButton;
    EditText passwordEditText;
    Button loginButton;
    Button getInfoButton;
    Button logoutButton;
    ru.rutoken.Pkcs11Caller.Pkcs11 pkcs11;


    //variables
    Map<String, Token> tokens = Collections.synchronizedMap(new HashMap<String, Token>());
    List<Token> tokenIndexes = new ArrayList<Token>();
    Token selectedToken = null;

    protected void Log(String line) {
        debugPrint.setText(debugPrint.getText()+line+"\n");
        int scrollY = debugPrint.getLineCount()*debugPrint.getLineHeight() - debugPrint.getHeight();
        if(scrollY > 0)
            debugPrint.scrollTo(0,scrollY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        debugPrint = (TextView) findViewById(R.id.debugPrint);
        tokenSpinner = (Spinner) findViewById(R.id.tokenList);
        updateListButton = (Button)findViewById(R.id.updateListButton);
        passwordEditText = (EditText)findViewById(R.id.passwordEditText);
        loginButton = (Button)findViewById(R.id.loginButton);
        getInfoButton = (Button)findViewById(R.id.getInfoButton);
        logoutButton = (Button)findViewById(R.id.logoutButton);

        Log(getText(R.string.welcome).toString());
        Log("Press Refresh to update token list");
        updateListButton.setText(getText(R.string.refresh).toString());
        loginButton.setText(getText(R.string.login).toString());
        getInfoButton.setText(getText(R.string.tokenInfo).toString());
        logoutButton.setText(getText(R.string.logout).toString());
        debugPrint.setMovementMethod(new ScrollingMovementMethod());


//        int rv = RtPkcs11Library.getInstance().C_Initialize(null);
//        if (rv != 0)
//        {
//            Log("C_Initialize failed, code error: " + Integer.toHexString(rv) + "\n");
//            return;
//        }
        pkcs11 = new ru.rutoken.Pkcs11Caller.Pkcs11(this.getApplicationContext());

//        getTokensInfo();
//        spinnerUpdate();


        setupUI(findViewById(R.id.parentLayout));
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void setupUI(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard((Activity)(v.getContext()));
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        int rv = RtPkcs11Library.getInstance().C_Finalize(null);
        if (rv != 0) {
            Log("C_Finalize failed, code error: " + Integer.toHexString(rv) + "\n");
            return;
        }
    }

    public void onUpdateListButtonClick(View view)
    {

        switch ( view.getId() )
        {
            case R.id.updateListButton:
                getTokensInfo();
                spinnerUpdate();
            default:
                break;
        }
    }

    public void onLoginButtonClick(View view) {
        switch ( view.getId() )
        {
            case R.id.loginButton:
                String pinCode = passwordEditText.getText().toString();
                byte[] pin = pinCode.getBytes(Charset.forName("US-ASCII"));
                login(pin);
            default:
                break;
        }
    }

    public void onLogoutButtonClick(View view) {
        switch ( view.getId() )
        {
            case R.id.logoutButton:
                logout();
            default:
                break;
        }
    }

    public void onGetInfoClick(View view) {
        switch ( view.getId() )
        {
            case R.id.getInfoButton:
                getInfo();
            default:
                break;
        }
    }

    protected void addToken(int slotid, CK_TOKEN_INFO info) {
        String serial = ByteArrayConverter.byteArrayToString(info.serialNumber);

        if(!tokens.containsKey(serial)) {
            Token t = new Token(slotid, info);
            tokens.put(serial,t);
        }
        else
            tokens.get(serial).isUpdated = true;
    }

    protected void removeTokens() {
        for(Token t : tokens.values()) {
            if(t.isUpdated == false)
            {
                tokens.remove(ByteArrayConverter.byteArrayToString(t.info.serialNumber));
                if (t == selectedToken)
                    selectedToken = null;
            }
        }
    }

    protected void login(byte[] pin) {
        Log("Login");
        if(null == selectedToken) {
            Log("Token not selected; select token to work with first");
            return;
        }
        try {
            IntByReference phSession = new IntByReference();
            int rv = RtPkcs11Library.getInstance().C_OpenSession(selectedToken.slotid, Pkcs11Constants.CKF_SERIAL_SESSION, null, null, phSession);
            if(rv != Pkcs11Constants.CKR_OK) {
                throw new InternalException("Failed to open session. Error: "+Integer.toHexString(rv));
            }
            selectedToken.hSession = phSession.getValue();

            rv = RtPkcs11Library.getInstance().C_Login(selectedToken.hSession, Pkcs11Constants.CKU_USER, pin, pin.length);
            if(rv != Pkcs11Constants.CKR_OK) {
                if(rv != Pkcs11Constants.CKR_USER_ALREADY_LOGGED_IN) {
                    if(selectedToken.hSession != -1)
                        RtPkcs11Library.getInstance().C_CloseSession(selectedToken.hSession);
                    selectedToken.hSession = -1;
                }
                throw new InternalException("Failed to login. Error: "+Integer.toHexString(rv));
            }
        }
        catch(InternalException e) {
            Log(e.getMessage());
            return;
        }
    }

    protected void logout() {
        Log("Logout");
        if(null == selectedToken) {
            Log("Token not selected; select token to work with first");
            return;
        }
        try {
            if(selectedToken.hSession == -1)
                throw new InternalException("Token not logged in; log in first");

            int rv = RtPkcs11Library.getInstance().C_Logout(selectedToken.hSession);
            if(rv != Pkcs11Constants.CKR_OK) {
                throw new InternalException("Failed to logout. Error: "+Integer.toHexString(rv));
            }
        }
        catch(InternalException e)
        {
            Log(e.getMessage());
            return;
        }
        finally {
            if(selectedToken.hSession != -1)
                RtPkcs11Library.getInstance().C_CloseSession(selectedToken.hSession);
            selectedToken.hSession = -1;
        }
    }

    protected void getInfo() {
        if(null == selectedToken) {
            Log("Token not selected; select token to work with first");
            return;
        }
        Log("Token info:");
        Log("Label: "+ ByteArrayConverter.byteArrayToString(selectedToken.info.label));
        Log("Manufacturer: "+ ByteArrayConverter.byteArrayToString(selectedToken.info.manufacturerID));
        Log("Model: "+ ByteArrayConverter.byteArrayToString(selectedToken.info.model));
        Log("Serial Number: "+ ByteArrayConverter.byteArrayToString(selectedToken.info.serialNumber));
        Log("Total memory: "+ String.valueOf(selectedToken.info.ulTotalPublicMemory+selectedToken.info.ulTotalPrivateMemory));
        Log("Free memory: "+ String.valueOf(selectedToken.info.ulFreePublicMemory+selectedToken.info.ulFreePrivateMemory));
    }

    private void getTokensInfo()
    {
        IntByReference slotsCount = new IntByReference();
        slotsCount.setValue(5);
        int[] pSlotList = new int[5];

        for(Token t:tokens.values()) {
            t.isUpdated = false;
        }

        try
        {
            int rv = RtPkcs11Library.getInstance().C_GetSlotList(true, null, slotsCount);
            if (rv != 0)
            {
                throw new InternalException("C_GetSlotList failed, code error: " + Integer.toHexString(rv) + "\n");
            }

            if (slotsCount.getValue() == 0)
            {
                throw new InternalException("No tokens availible");
            }
            else
            {
                rv = RtPkcs11Library.getInstance().C_GetSlotList(true, pSlotList, slotsCount);
                if (rv != 0)
                {
                    throw new InternalException("C_GetSlotList failed, code error: " + Integer.toHexString(rv) + "\n");
                }

                int bla = slotsCount.getValue();
                for (int i=0; i < slotsCount.getValue(); i++)
                {
                    CK_TOKEN_INFO tokenInfo = new CK_TOKEN_INFO();
                    rv = RtPkcs11Library.getInstance().C_GetTokenInfo(pSlotList[i], tokenInfo);
                    if (rv != 0) {
                        throw new InternalException("C_GetTokenInfo failed, code error: " + Integer.toHexString(rv) +"\n");
                    }
                    addToken(pSlotList[i],tokenInfo);
                }
            }
        } catch (InternalException e) {
            Log(e.getMessage());
        }
        finally {
            removeTokens();
        }
    }

    protected void spinnerUpdate()
    {
        if (tokens == null)
        {
            return;
        }

        ArrayList<String> tokenstrs = new ArrayList<String>();

        tokenIndexes.clear();
        int i = 0;
        int selectedTokenPosition = 0;
        for(Token t: tokens.values())
        {
            tokenIndexes.add(t);
            if(t == selectedToken)
                selectedTokenPosition = i;
            ++i;
            tokenstrs.add(ByteArrayConverter.byteArrayToString(t.info.model) +" "+ ByteArrayConverter.byteArrayToString(t.info.serialNumber));
        }

        if(tokenstrs.size() == 0)
            tokenstrs.add(getString(R.string.no_token));

        try
        {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tokenstrs);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            tokenSpinner.setAdapter(arrayAdapter);
            tokenSpinner.setPrompt("Token List");
            tokenSpinner.setSelection(selectedTokenPosition);
            tokenSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if(i < tokenIndexes.size())
                        selectedToken = tokenIndexes.get(i);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

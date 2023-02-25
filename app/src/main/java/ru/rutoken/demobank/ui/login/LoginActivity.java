/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See https://download.rutoken.ru/License_Agreement.pdf
 * All Rights Reserved.
 */

package ru.rutoken.demobank.ui.login;


import static javax.crypto.Cipher.*;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;


import java.util.concurrent.Executor;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import ru.rutoken.demobank.R;
import ru.rutoken.demobank.pkcs11caller.Token;
import ru.rutoken.demobank.pkcs11caller.TokenManager;
import ru.rutoken.demobank.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.demobank.ui.Pkcs11CallerActivity;
import ru.rutoken.demobank.ui.TokenManagerListener;
import ru.rutoken.demobank.ui.main.MainActivity;
import ru.rutoken.demobank.ui.payment.PaymentsActivity;
import ru.rutoken.demobank.utils.Pkcs11ErrorTranslator;
import ru.rutoken.demobank.database.AppDataBase;
import ru.rutoken.demobank.database.User;

public class LoginActivity extends Pkcs11CallerActivity {

    /**
     * Data that we have received from the server to do a challenge-response authentication
     */
    public final String tokenSerialNumber= TokenManagerListener.getInstance(this).getTokenSerial(); //получаю номер токена
    private final String KEY_NAME = tokenSerialNumber;
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String FORWARD_SLASH = "/";
    private static final String SIGN_DATA = "sign me";

    // GUI
    private Button mLoginButton,mReturnBioDialog;
    private EditText mPinEditText;
    private TextView mAlertTextView;
    private ProgressBar mLoginProgressBar;
    private Dialog mOverlayDialog;
    private CheckBox mCheckBox;
    private boolean ButtonChecked;
    private boolean DecryptDialog,DecryptError = false;

    private String mTokenSerial = TokenManagerListener.NO_TOKEN;
    private String mCertificateFingerprint = TokenManagerListener.NO_FINGERPRINT;
    private Token mToken = null;

    //Initialize KeyGen and Cipher
    private void generateSecretKey(KeyGenParameterSpec keyGenParameterSpec) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
        keyGenerator.init(keyGenParameterSpec);
        keyGenerator.generateKey();
    }
    private SecretKey getSecretKey() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        // Before the keystore can be accessed, it must be loaded.
        keyStore.load(null);
        return ((SecretKey)keyStore.getKey(KEY_NAME, null));
    }
    private Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return getInstance(KeyProperties.KEY_ALGORITHM_AES + FORWARD_SLASH + KeyProperties.BLOCK_MODE_CBC + FORWARD_SLASH + KeyProperties.ENCRYPTION_PADDING_PKCS7);
    }

    //BiometricManager
    private BiometricManager biometricManager() {
        return BiometricManager.from(this);
    }
    private boolean isBiometricCompatibleDevice() {
        if (biometricManager().canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG |
                BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS) {
            return true;
        }
        else return false;
    }
    BiometricPrompt.PromptInfo.Builder dialogMetric()
    {
        return new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.login_biometric))
                .setSubtitle(getString(R.string.use_fingerprint));
    }
    private void CreateOnlyOneSecretKey(){
        try {
            if(getSecretKey() == null){
                try {
                    generateSecretKey(new KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                            .setUserAuthenticationRequired(true)
                            .build());
                } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {e.printStackTrace();}
            }
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException | UnrecoverableKeyException e) {e.printStackTrace();}
    }

    //Biometric authentication dialog for encryption and decryption
    private void BioAuthEncrypt(){
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if(errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON){
                    startActivity(new Intent(LoginActivity.this, PaymentsActivity.class)
                            .putExtra(MainActivity.EXTRA_TOKEN_SERIAL, mTokenSerial)
                            .putExtra(MainActivity.EXTRA_CERTIFICATE_FINGERPRINT, mCertificateFingerprint));
                }
                Toast.makeText(LoginActivity.this,getString(R.string.error_authentication) + errString,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                byte[] encryptedInfo = new byte[0];
                byte[] pin = (mPinEditText.getText().toString()).getBytes();
                try {encryptedInfo = result.getCryptoObject().getCipher().doFinal(pin);}
                catch (BadPaddingException | IllegalBlockSizeException e) {e.printStackTrace();}
                boolean checkedUser = checkUser(tokenSerialNumber);
                if(!checkedUser){
                    saveNewUser(tokenSerialNumber, encryptedInfo, result.getCryptoObject().getCipher().getIV());
                } else{
                    deleteUser(tokenSerialNumber);
                    saveNewUser(tokenSerialNumber, encryptedInfo, result.getCryptoObject().getCipher().getIV());
                }
                startActivity(new Intent(LoginActivity.this, PaymentsActivity.class)
                        .putExtra(MainActivity.EXTRA_TOKEN_SERIAL, mTokenSerial)
                        .putExtra(MainActivity.EXTRA_CERTIFICATE_FINGERPRINT, mCertificateFingerprint));
                Toast.makeText(LoginActivity.this,getString(R.string.success_authentication),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(LoginActivity.this,getString(R.string.fail_authentication),Toast.LENGTH_SHORT).show();
            }
        });
        if(mCheckBox.isChecked()){
            Cipher cipherEncrypt = null;
            try {cipherEncrypt = getCipher();}
            catch (NoSuchPaddingException | NoSuchAlgorithmException e) {e.printStackTrace();}
            SecretKey secretKey = null;
            try {secretKey = getSecretKey();}
            catch (KeyStoreException | IOException | UnrecoverableKeyException | NoSuchAlgorithmException | CertificateException e) {e.printStackTrace();}
            try {if (cipherEncrypt != null) {cipherEncrypt.init(ENCRYPT_MODE,secretKey);}}
            catch (InvalidKeyException e) {throw new RuntimeException(e.getMessage());}
            BiometricPrompt.PromptInfo.Builder promptInfo = dialogMetric();
            promptInfo.setNegativeButtonText(getString(R.string.cancel));
            if (cipherEncrypt != null) {biometricPrompt.authenticate(promptInfo.build(), new BiometricPrompt.CryptoObject(cipherEncrypt));}
        }
        else{
            startActivity(new Intent(LoginActivity.this, PaymentsActivity.class)
                    .putExtra(MainActivity.EXTRA_TOKEN_SERIAL, mTokenSerial)
                    .putExtra(MainActivity.EXTRA_CERTIFICATE_FINGERPRINT, mCertificateFingerprint));
        }
    }
    private void BioAuthDecrypt(){
        DecryptError = true;
        byte[] CodeName = getUserCodename(tokenSerialNumber);
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPromptUserExist = new BiometricPrompt(LoginActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if(errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON){
                    mReturnBioDialog.setEnabled(false);
                    mReturnBioDialog.setVisibility(View.GONE);
                }
                Toast.makeText(LoginActivity.this,getString(R.string.error_authentication) + errString,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                byte[] decryptedInfo = new byte[0];
                try {decryptedInfo = result.getCryptoObject().getCipher().doFinal(CodeName);}
                catch (Exception e) {e.printStackTrace();}
                Toast.makeText(LoginActivity.this,getString(R.string.success_authentication),Toast.LENGTH_SHORT).show();
                login(mToken, new String(decryptedInfo), mCertificateFingerprint, SIGN_DATA.getBytes());
            }
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(LoginActivity.this,getString(R.string.fail_authentication),Toast.LENGTH_SHORT).show();
            }
        });
        Cipher cipherDecrypt = null;
        try {cipherDecrypt = getCipher();}
        catch (NoSuchPaddingException | NoSuchAlgorithmException e) {e.printStackTrace();}
        SecretKey secretKey = null;
        try {secretKey = getSecretKey();}
        catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException | UnrecoverableKeyException e) {e.printStackTrace();}
        try {
            if (cipherDecrypt != null) {
                cipherDecrypt.init(DECRYPT_MODE, secretKey ,new IvParameterSpec(getUserIv(tokenSerialNumber)));
            }
        }
        catch (InvalidKeyException | InvalidAlgorithmParameterException e) {throw new RuntimeException(e.getMessage());}
        BiometricPrompt.PromptInfo.Builder promptInfo = dialogMetric();
        promptInfo.setNegativeButtonText(getString(R.string.cancel));
        assert cipherDecrypt != null;
        biometricPromptUserExist.authenticate(promptInfo.build(), new BiometricPrompt.CryptoObject(cipherDecrypt));
    }

    //Functions of DAO
    private void saveNewUser(String SerialNumber, byte[] Codename, byte[] iv) {
        AppDataBase db  = AppDataBase.getdbInstance(this.getApplicationContext());
        User user = new User();
        user.TokenSerialNumber = SerialNumber;
        user.codename = Codename;
        user.iv = iv;
        db.userDao().insertUser(user);
        finish();
    }
    private boolean checkUser(String Number){
        AppDataBase db  = AppDataBase.getdbInstance(this.getApplicationContext());
        User FoundUser = db.userDao().getNumber(Number);
        return FoundUser != null;
    }
    private byte[] getUserCodename(String Number){
        AppDataBase db  = AppDataBase.getdbInstance(this.getApplicationContext());
        User FoundUser = db.userDao().getNumber(Number);
        return FoundUser.codename;
    }
    private byte[] getUserIv(String Number){
        AppDataBase db  = AppDataBase.getdbInstance(this.getApplicationContext());
        User FoundUser = db.userDao().getNumber(Number);
        return FoundUser.iv;
    }
    private void deleteUser(String Username){
        AppDataBase db  = AppDataBase.getdbInstance(this.getApplicationContext());
        db.userDao().deleteUser(String.valueOf(Username));
    }


    @Override
    public String getActivityClassIdentifier() {
        return getClass().getName();
    }

    private void showLogonStarted() {
        mLoginProgressBar.setVisibility(View.VISIBLE);
        mLoginButton.setEnabled(false);
        mOverlayDialog.show();
    }

    private void showLogonFinished() {
        mLoginProgressBar.setVisibility(View.GONE);
        mLoginButton.setEnabled(true);
        mOverlayDialog.dismiss();
    }

    @Override
    protected void manageTokenOperationError(@Nullable Pkcs11Exception exception) {
        mToken.clearPin();
        String message = (exception == null) ? getString(R.string.error) : Pkcs11ErrorTranslator.getInstance(this).messageForRV(exception.getErrorCode());

        mAlertTextView.setText(message);
        if(DecryptError){
            mAlertTextView.setText(getString(R.string.password_changed));
            mReturnBioDialog.setEnabled(false);
            mReturnBioDialog.setVisibility(View.GONE);
        }
        showLogonFinished();
        mPinEditText.setText("");
    }

    @Override
    protected void manageTokenOperationCanceled() {
        showLogonFinished();
    }

    @Override
    protected void manageTokenOperationSucceed() {
        if((ButtonChecked) && isBiometricCompatibleDevice()){
            BioAuthEncrypt();
            showLogonFinished();
            ButtonChecked = false;
        } else{
            startActivity(new Intent(LoginActivity.this, PaymentsActivity.class)
                    .putExtra(MainActivity.EXTRA_TOKEN_SERIAL, mTokenSerial)
                    .putExtra(MainActivity.EXTRA_CERTIFICATE_FINGERPRINT, mCertificateFingerprint));
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        Intent intent = getIntent();
        mTokenSerial = intent.getStringExtra(MainActivity.EXTRA_TOKEN_SERIAL);
        mCertificateFingerprint = intent.getStringExtra(MainActivity.EXTRA_CERTIFICATE_FINGERPRINT);
        mToken = TokenManager.getInstance().getTokenBySerial(mTokenSerial);
        CreateOnlyOneSecretKey();
        boolean checkedUser = checkUser(tokenSerialNumber);
        if(checkedUser && isBiometricCompatibleDevice()){
            BioAuthDecrypt();
            DecryptDialog = true;
        }
        if (mToken == null) {
            Toast.makeText(this, R.string.rutoken_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mOverlayDialog = new Dialog(this, android.R.style.Theme_Panel);
        mOverlayDialog.setCancelable(false);

        setupActionBar();
        setupUI();
        if(isBiometricCompatibleDevice()){
            mCheckBox.setVisibility(View.VISIBLE);}
        if(DecryptDialog){
            mReturnBioDialog.setEnabled(true);
            mReturnBioDialog.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart(Bundle savedInstanceState){
        super.onStart(savedInstanceState);
        setupUI();
    }

    private void setupActionBar() {
        View view = getLayoutInflater().inflate(R.layout.actionbar_layout, null);

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);

        /* Custom actionbar */
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(view, params);
        }
    }

    private void setupUI() {
        mLoginButton = findViewById(R.id.loginB);
        mPinEditText = findViewById(R.id.pinET);
        mAlertTextView = findViewById(R.id.alertTV);
        mLoginProgressBar = findViewById(R.id.loginPB);
        mCheckBox = findViewById(R.id.CheckBox);
        mReturnBioDialog = findViewById(R.id.returnBioDialog);

        mLoginProgressBar.setVisibility(View.GONE);
        mLoginButton.setEnabled(false);

        mPinEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (mPinEditText.getText().toString().isEmpty()) {
                    mLoginButton.setEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mPinEditText.getText().toString().isEmpty()) {
                    mLoginButton.setEnabled(false);
                } else {
                    mLoginButton.setEnabled(true);
                }
            }
        });
        mPinEditText.requestFocus();

        mLoginButton.setOnClickListener(view -> {
            TokenManagerListener.getInstance(this).resetWaitForToken();
            showLogonStarted();
            ButtonChecked = true;
            // Certificate and sign data are used for a challenge-response authentication.
            login(mToken, mPinEditText.getText().toString(), mCertificateFingerprint, SIGN_DATA.getBytes());
        });
        mReturnBioDialog.setOnClickListener(view -> {
            BioAuthDecrypt();
        });
    }
}

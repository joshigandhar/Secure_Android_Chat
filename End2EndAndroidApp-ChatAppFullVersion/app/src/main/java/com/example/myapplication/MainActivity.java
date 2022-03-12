package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.security.CryptoPrimitive;
import java.security.KeyPair;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity implements ValueEventListener{
    EditText InputMsg;
    EditText EncryptedMsg;
    EditText DecryptedMsg;

    String inputmsg;
    String encryptedmsg;
    String decryptedmsg;

    byte[] privatekey;
    byte[] publickey;
    byte[] AESkey;

    byte[] encryptedKey;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mMessageReference;
    private DatabaseReference mMessagevalueReference;
    private String stringforserver;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InputMsg = findViewById(R.id.InputMsg);
        EncryptedMsg = findViewById(R.id.EncryptMsg);
        DecryptedMsg = findViewById(R.id.DecryptMsg);

        InputMsg.setHint("Input Message Here");
        EncryptedMsg.setHint("Encrypted Message Here");
        DecryptedMsg.setHint("Decrypted Message Here");
        Button EncryptBtn = findViewById(R.id.Encryptbtn);
        EncryptBtn.setText("Encrypt Message");
        Button DecryptBtn = findViewById(R.id.Decryptbtn);
        DecryptBtn.setText("Decrypt Message");

        mDatabase = FirebaseDatabase.getInstance();
        mMessageReference = mDatabase.getReference();
        Date date =new Date();
        mMessagevalueReference = mMessageReference.child(Long.toString(date.getTime()));




        try {
            KeyPair RSAkeypair = CrptoUtil.generateKeyPair();
            publickey = RSAkeypair.getPublic().getEncoded();
            privatekey = RSAkeypair.getPrivate().getEncoded();
        }
        catch (Exception e) {
            Log.e("MyApplication", e.getMessage());
        }
        EncryptBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view)
            {
                inputmsg = InputMsg.getText().toString();
                //System.out.println(inputmsg);
                //System.out.println(inputmsg.getBytes());
                try {
                    encryptedmsg = CrptoUtil.EncryptDriver(inputmsg);
                    AESkey = CrptoUtil.getAESkey();
                    encryptedKey = CrptoUtil.encryptkey(AESkey, publickey);
                } catch (Exception e) {
                    //e.printStackTrace();
                    Log.e("MyApplication", e.getMessage());
                }
                //System.out.println(encryptedmsg);
                EncryptedMsg.setText(encryptedmsg);
                Date date =new Date();
                mMessagevalueReference = mMessageReference.child(Long.toString(date.getTime()));
                mMessagevalueReference.setValue(encryptedmsg);
                hideSoftKeyboard(MainActivity.this, view);
            }
        });


        DecryptBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view)
            {

                try {
                    AESkey = CrptoUtil.decryptkey(privatekey, encryptedKey);
                    decryptedmsg = CrptoUtil.DecryptionDriver(encryptedmsg, AESkey);
                } catch (Exception e) {
                    Log.e("MyApplication", e.getMessage());
                }
                DecryptedMsg.setText(decryptedmsg);
            }
        });
    }

    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    public void onStart() {

        super.onStart();
        mMessagevalueReference.addValueEventListener(this);
    }
}

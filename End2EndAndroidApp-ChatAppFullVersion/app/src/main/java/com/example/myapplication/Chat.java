package com.example.myapplication;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class Chat extends AppCompatActivity implements ValueEventListener {
    LinearLayout layout;
    RelativeLayout layout_2;
    Button sendButton;
    EditText messageArea;
    ScrollView scrollView;
    Firebase reference1, reference2;


    byte[] privatekey;
    byte[] publickey;
    byte[] AESkey;
    byte[] encryptedKey;

    String encryptedmsg;
    String decryptedmsg;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        layout = findViewById(R.id.layout1);
        layout_2 = findViewById(R.id.layout2);
        sendButton = findViewById(R.id.sendButton);
        messageArea = findViewById(R.id.messageArea);
        scrollView = findViewById(R.id.scrollView);

        Firebase.setAndroidContext(this);
        reference1 = new Firebase("https://messaging-2b337.firebaseio.com/messages/" + UserDetails.username + "_" + UserDetails.chatWith);
        reference2 = new Firebase("https://messaging-2b337.firebaseio.com/messages/" + UserDetails.chatWith + "_" + UserDetails.username);

        KeyPair RSAkeypair = null;
        try {
            RSAkeypair = CrptoUtil.generateKeyPair();
            byte[] publickeytosend = RSAkeypair.getPublic().getEncoded();
            SendPublicKey(publickeytosend);
            privatekey = RSAkeypair.getPrivate().getEncoded();
        } catch (Exception e) {
            Log.e("MyApplication", e.getMessage());
        }


        sendButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();

                if(!messageText.equals("")) {
                    try {
                        encryptedmsg = CrptoUtil.EncryptDriver(messageText);
                        AESkey = CrptoUtil.getAESkey();
                        encryptedKey = CrptoUtil.encryptkey(AESkey, publickey);

                        Map<String, String> map_receiver = new HashMap<String, String>();

                        map_receiver.put("message", encryptedmsg);
                        map_receiver.put("user", UserDetails.username);
                        map_receiver.put("key", Base64.getEncoder().encodeToString(encryptedKey));
                        map_receiver.put("type", "Message");

                        reference2.push().setValue(map_receiver);
                        addMessageBox(messageText, 2);
                        messageArea.setText("");

                    } catch (Exception e) {
                        Log.e("MyApplication", e.getMessage());
                    }
                }
            }
        });

        reference1.addChildEventListener(new ChildEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String message = map.get("message").toString();

                String userName = map.get("user").toString();
                if(map.containsKey("type"))
                {
                    if(map.get("type").toString().equalsIgnoreCase("publicKey")){
                        publickey = Base64.getDecoder().decode(message);
                    }
                    else //message
                    {
                        try {
                            String keystring = map.get("key").toString();
                            byte[] encryptedkey = Base64.getDecoder().decode(keystring);
                            byte[] aeskey = CrptoUtil.decryptkey(privatekey, encryptedkey);
                            String decMsg = CrptoUtil.DecryptionDriver(message, aeskey);
                            addMessageBox(decMsg, 1);
                        } catch (Exception e) {
                            Log.e("MyApplication", e.getMessage());
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void addMessageBox(String message, int type){
        TextView textView = new TextView(Chat.this);
        textView.setText(message);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 7.0f;

        if(type == 1) {
            lp2.gravity = Gravity.LEFT;
            textView.setBackgroundResource(R.drawable.bubble_in);
            setTextViewStyle(textView);
        }
        else{
            lp2.gravity = Gravity.RIGHT;
            textView.setBackgroundResource(R.drawable.bubble_out);
            setTextViewStyle(textView);
        }
        textView.setLayoutParams(lp2);
        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }

    @Override
    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void SendPublicKey(byte[] key)
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("message", Base64.getEncoder().encodeToString(key));
        map.put("user", UserDetails.username);
        map.put("type", "publicKey");
        reference2.push().setValue(map);
    }

    private void setTextViewStyle(TextView view)
    {
        view.setTextSize(18);
        int padding_in_dp = 6;  // 6 dps
        float scale = getResources().getDisplayMetrics().density;
        int padding_in_px = (int) (padding_in_dp * scale + 0.5f);
        view.setPadding(padding_in_px,padding_in_px,padding_in_px,padding_in_px);
    }
}
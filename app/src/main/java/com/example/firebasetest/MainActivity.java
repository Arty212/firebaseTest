package com.example.firebasetest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference ref;
    private DatabaseReference refUser;

    private TextView text;
    private Button btn;
    private Button btnLogout;

    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = findViewById(R.id.text);
        btn = findViewById(R.id.btn);
        btnLogout = findViewById(R.id.btn_logout);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        ref = FirebaseDatabase.getInstance().getReference();

        if (user == null)
            login();
        else
            syncData();


        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                login();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score++;
                text.setText(String.valueOf(score));
                refUser.child("score").setValue(score);
            }
        });
    }

    private void login() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());


        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                100);
    }

    private void syncData() {
        refUser = ref.child("users").child(user.getUid());
        ref.child("users").child(user.getUid()).child("score").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    score = Integer.parseInt(dataSnapshot.getValue().toString());
                    text.setText(String.valueOf(score));
                } else {
                    ref.child("users").child(user.getUid()).child("score").setValue(0);
                    ref.child("users").child(user.getUid()).child("email").setValue(user.getEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            user = FirebaseAuth.getInstance().getCurrentUser();
            syncData();
        }
    }
}

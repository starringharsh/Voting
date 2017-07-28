package com.fsp.starringharsh.voting;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class Vote extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    RadioGroup rgcand;
    LinearLayout ll;
    TextView tv;
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = Menu.class.getName();
    //FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference myRef = rootRef.child("candidates");
    DatabaseReference writeRef = rootRef.child("votes");
    FirebaseUser user;
    String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);

        rgcand = (RadioGroup) findViewById(R.id.rgCandidate);
        ll = (LinearLayout) findViewById(R.id.llVoting);
        tv = (TextView) findViewById(R.id.tvVote);

        myRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            String candName = String.valueOf(dsp.child("name").getValue()); //add result into array list
                            RadioButton rb = new RadioButton(Vote.this);
                            rb.setText(candName);
                            rgcand.addView(rb);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                        Toast.makeText(Vote.this, "Unable to connect to the Database.", Toast.LENGTH_SHORT).show();
                    }
                });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();

        user = mAuth.getCurrentUser();
        userID = user.getUid();

        checkPrev();

        /*if (checkPrev())
        {
            ll.setVisibility(View.VISIBLE);
        }
        else
        {
            Toast.makeText(this, "Vote already casted.", Toast.LENGTH_SHORT).show();
            tv.setText("Your vote has already been casted.");
        }*/

        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.bSubmitVote).setOnClickListener(this);

    }

    void checkPrev()
    {
        DatabaseReference chk = writeRef.child(userID);
        final String[] casted = new String[1];
        chk.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                if (value == null)
                {
                    ll.setVisibility(View.VISIBLE);
                }
                else
                {
                    tv.setText("Vote Already Casted");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        //if(casted[0] == null)
            //return true;
        //else
            //return false;
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();
        startActivity(new Intent(Vote.this, PhoneAuthActivity.class));
        finish();

        // Google sign out
        /*Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Toast.makeText(Vote.this, "Signed Out Successfully.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Vote.this, GoogleSignInActivity.class));
                        finish();
                    }
                });*/
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.

        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.bSubmitVote) {
            if (userID == null)
                Toast.makeText(this, "User Null", Toast.LENGTH_SHORT).show();
            String selected = ((RadioButton)findViewById(rgcand.getCheckedRadioButtonId())).getText().toString();
            Toast.makeText(this, "Thank You For Your Precious Vote.", Toast.LENGTH_SHORT).show();
            writeRef.child(userID).setValue(selected);
            onBackPressed();

        } else if (i == R.id.bResults) {
            //view results activity
        } else if (i == R.id.sign_out_button) {
            signOut();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Vote.this, Menu.class);
        startActivity(intent);
        finish();
    }

}

package com.fsp.starringharsh.voting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowResults extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    DatabaseReference databaseCandidate = FirebaseDatabase.getInstance().getReference("candidates");
    DatabaseReference databaseVotes = FirebaseDatabase.getInstance().getReference("votes");

    ListView listViewCandidate;
    List<Candidate> candidateList;
    Map<String, Long> candidateMap;

    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = ShowResults.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_results);

        candidateMap = new HashMap<String, Long>();
        candidateList = new ArrayList<>();
        listViewCandidate = (ListView) findViewById(R.id.lvResult);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.sign_out_button).setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        databaseVotes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                candidateList.clear();
                candidateMap.clear();
                for(DataSnapshot voteSnapshot: dataSnapshot.getChildren())
                {
                    String temp = voteSnapshot.getValue().toString();

                    if(candidateMap.get(temp.toString()) != null)
                    {
                        Long l = candidateMap.get(temp.toString()) + 1L;
                        candidateMap.put(temp, l);
                    }
                    else
                    {
                        candidateMap.put(temp, 1L);
                    }

                }
                for(Map.Entry<String, Long> i : candidateMap.entrySet())
                {
                    Candidate candidate = new Candidate(i.getKey(), i.getValue());
                    candidateList.add(candidate);
                }
                CandidateList adapter = new CandidateList(ShowResults.this, candidateList);
                listViewCandidate.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Toast.makeText(ShowResults.this, "Signed Out Successfully.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ShowResults.this, GoogleSignInActivity.class));
                        finish();
                    }
                });
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
        if (i == R.id.sign_out_button) {
            signOut();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ShowResults.this, Menu.class);
        startActivity(intent);
        finish();
    }
}

class Candidate
{
    String candName;
    Long candCount;

    public Candidate() {
    }

    public Candidate(String candName, Long candCount) {
        this.candName = candName;
        this.candCount = candCount;
    }
}

class CandidateList extends ArrayAdapter<Candidate>
{

    private Activity context;
    private List<Candidate> candidateList;

    public CandidateList(Activity context, List<Candidate> candidateList) {
        super(context, R.layout.result_list, candidateList);
        this.context = context;
        this.candidateList = candidateList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.result_list, null, true);
        TextView tvCand = (TextView) listViewItem.findViewById(R.id.tvCand);
        TextView tvCount = (TextView) listViewItem.findViewById(R.id.tvCount);

        Candidate candidate = candidateList.get(position);

        tvCand.setText(candidate.candName);
        tvCount.setText("" + candidate.candCount + " Votes");

        return listViewItem;
    }
}
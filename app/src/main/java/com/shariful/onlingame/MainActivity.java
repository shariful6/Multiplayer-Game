package com.shariful.onlingame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shariful.onlingame.Adapter.AdapterUser;
import com.shariful.onlingame.Model.UserOnline;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    Button playBtn,exitBtn;
    ImageView spinView;
    TextView pointsTv,spinCountTv, playerNameTv,scorePlayerTv;

    int degree =0;
    int degree_old =0;
    Random r;
    int score = 0;
    public static final float FACTOR =15f;

    String current_score ;
    int spin_limit =20;
    int spinCount=0;

    private DatabaseReference ref_user;
    private DatabaseReference reference;

    String myUid;
    String inviter_uid;
    String inviter_name;
    String universal_uid;

    String myName;
    int myScore;

    int flag=0;

    List<UserOnline> onlinesList;
    private AdapterUser adapterUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getSupportActionBar();

        ref_user = FirebaseDatabase.getInstance().getReference("userList");
        reference = FirebaseDatabase.getInstance().getReference("player_zone");

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        playBtn =findViewById(R.id.playBtn_id);
        exitBtn =findViewById(R.id.exitBtn_id);
        spinView = findViewById(R.id.spinner);
        pointsTv = findViewById(R.id.pointsTv_id);
        spinCountTv = findViewById(R.id.spinCountTv_id);
        playerNameTv = findViewById(R.id.playerNameTv_id);
        scorePlayerTv = findViewById(R.id.playerScoreTv_id);

        onlinesList = new ArrayList<>();
        r = new Random();

        current_score =currentNumber(360-(degree&360));

        playerNameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onlineListDialog();
            }
        });

        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                reference.child(universal_uid).child("left").setValue("1");
                playerNameTv.setText("Invite First!");
                scorePlayerTv.setText("0");
                playBtn.setEnabled(false);
                exitBtn.setEnabled(false);
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    flag=1;
                    play();

            }
        });

         getInviteList();
         getUserList();
         getMyInfo();

         ref_user.child(myUid).child("temp_score").setValue("0");
         playBtn.setEnabled(false);

    }

    private void checkState() {
        reference.child(universal_uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    int deg2 = Integer.parseInt(snapshot.child("hitter_degree").getValue(String.class));
                    String my_token = snapshot.child("my_token").getValue(String.class);
                    String state = snapshot.child("left").getValue(String.class);


                    try {
                        if (deg2!=0 && flag ==0){
                            playBtn.setEnabled(false);
                            play2(deg2);
                        }
                        else if(my_token.equals(myUid)){
                            playBtn.setEnabled(false);
                        }
                        else if(state.equals("1")){
                            playerNameTv.setText("player Left!");
                            scorePlayerTv.setText("0");
                            playBtn.setEnabled(false);
                            exitBtn.setEnabled(false);
                        }
                        else{
                            playBtn.setEnabled(true);
                        }


                    }catch (Exception e){

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUniversalUid(){
        ref_user.child(myUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    universal_uid = snapshot.child("universal_uid").getValue(String.class);
                    reference.child(universal_uid).child("left").setValue("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMyInfo() {
        ref_user.child(myUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    myName = snapshot.child("name").getValue(String.class);
                    myScore = Integer.parseInt(snapshot.child("temp_score").getValue(String.class));
                    pointsTv.setText(String.valueOf(myScore));
                    getSupportActionBar().setTitle(myName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Menu section
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id==R.id.invite_menu){
                onlineListDialog();

            }


        return super.onOptionsItemSelected(item);
    }

    private void onlineListDialog() {
        // Toast.makeText(this, "Points: "+a, Toast.LENGTH_SHORT).show();
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.online_user_view);
        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView_dialog_id);

        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adapterUser = new AdapterUser(MainActivity.this,onlinesList);
        recyclerView.setAdapter(adapterUser);
        adapterUser.notifyDataSetChanged();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("invite_list");
        reference.child(myUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                        String acceptation  = snapshot.child("acceptation").getValue(String.class);
                        String player_name  = snapshot.child("player_name").getValue(String.class);
                        String hisUid  = snapshot.child("player_uid").getValue(String.class);
                       try {
                           if (acceptation.equals("yes")){
                               playerNameTv.setText(player_name);
                               getPlayerScrore(hisUid);
                               deleteInvite();
                               getUniversalUid();
                               playBtn.setEnabled(true);
                               exitBtn.setEnabled(true);

                           }else {
                               playerNameTv.setText("Waiting for response...");
                           }
                       }catch (Exception e){

                       }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void deleteInvite() {

        Query fquery = FirebaseDatabase.getInstance().getReference("invite_list").orderByChild("inviter_uid").equalTo(myUid);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ds.getRef().removeValue(); // remove value from firebase where pId matched

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void deleteInvite2(String inviter_uid) {
        Query fquery = FirebaseDatabase.getInstance().getReference("invite_list").orderByChild("inviter_uid").equalTo(inviter_uid);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ds.getRef().removeValue(); // remove value from firebase where pId matched
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPlayerScrore(String hisUid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("userList");
        reference.child(hisUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String score = snapshot.child("temp_score").getValue(String.class);
                    scorePlayerTv.setText(score);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getInviteList(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("invite_list");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds:snapshot.getChildren()){
                        String player_uid = ds.child("player_uid").getValue(String.class);
                        inviter_uid = ds.child("inviter_uid").getValue(String.class);
                        String status = ds.child("acceptation").getValue(String.class);
                        inviter_name = ds.child("inviter_name").getValue(String.class);

                          try {
                              if (player_uid.equals(myUid) && status.equals("no")){
                                  buildDialog(MainActivity.this,inviter_uid).show();
                              }
                          }catch (Exception e){

                          }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public AlertDialog.Builder buildDialog(Context c,String inviter_uid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("invite_list");
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Invitation !!");
        builder.setMessage(inviter_name+" have invited you to play !");
        builder.setCancelable(false);
        builder.setPositiveButton("Accept?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                  reference.child(inviter_uid).child("acceptation").setValue("yes");
                  playerNameTv.setText(inviter_name);
                  getInviterScore();

                  ref_user.child(myUid).child("universal_uid").setValue(myUid);
                  ref_user.child(myUid).child("join").setValue("yes");
                  ref_user.child(inviter_uid).child("universal_uid").setValue(myUid);

                  getUniversalUid();
                  playBtn.setEnabled(true);
                  exitBtn.setEnabled(true);

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                   deleteInvite2(inviter_uid);
            }
        });

        return builder;
    }

    private void getInviterScore() {
        ref_user.child(inviter_uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String inviterScore = snapshot.child("temp_score").getValue(String.class);
                    scorePlayerTv.setText(inviterScore);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserList(){
        ref_user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    onlinesList.clear();
                    for (DataSnapshot ds:snapshot.getChildren()){
                        UserOnline userOnline = ds.getValue(UserOnline.class);
                        if (!userOnline.getUid().equals(myUid)){
                            onlinesList.add(userOnline);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void play() {

        checkState();

        degree_old = degree%360;
        degree =r.nextInt(3600)+720;

        reference.child(universal_uid).child("hitter_degree").setValue(String.valueOf(degree));

        RotateAnimation rotateAnimation = new RotateAnimation(degree_old,degree,
                RotateAnimation.RELATIVE_TO_SELF,0.5f,RotateAnimation.RELATIVE_TO_SELF,0.5f);
        rotateAnimation.setDuration(3600);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setInterpolator(new DecelerateInterpolator());

        MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.soundspinner);

        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mediaPlayer.start();
                pointsTv.setText("Waiting");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                reference.child(universal_uid).child("hitter_degree").setValue("0");
                mediaPlayer.stop();
                openDialog(currentNumber(360-(degree%360)));
                //  pointsTv.setText(currentNumber(360-(degree%360)));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        spinView.startAnimation(rotateAnimation);


    }

    private String currentNumber(int degrees) {

        String text = "";
        if (degrees >= (FACTOR * 1) && degrees < (FACTOR * 3)) {
            text = "250";
        }
        if (degrees >= (FACTOR * 3) && degrees < (FACTOR * 5)) {
            text = "350";
        }
        if (degrees >= (FACTOR * 5) && degrees < (FACTOR * 7)) {
            text = "500";
        }
        if (degrees >= (FACTOR * 7) && degrees < (FACTOR * 9)) {
            text = "01";
        }
        if (degrees >= (FACTOR * 9) && degrees < (FACTOR * 11)) {
            text = "05";
        }
        if (degrees >= (FACTOR * 11) && degrees < (FACTOR * 13)) {
            text = "10";
        }
        if (degrees >= (FACTOR * 13) && degrees < (FACTOR * 15)) {
            text = "20";
        }
        if (degrees >= (FACTOR * 15) && degrees < (FACTOR * 17)) {
            text = "50";
        }
        if (degrees >= (FACTOR * 17) && degrees < (FACTOR * 19)) {
            text = "75";
        }
        if (degrees >= (FACTOR * 19) && degrees < (FACTOR * 21)) {
            text = "100";
        }
        if (degrees >= (FACTOR * 21) && degrees < (FACTOR * 23)) {
            text = "150";
        }
        if ((degrees >= (FACTOR * 23) && degrees < 360) || (degrees >= 0 && degrees < (FACTOR * 1))) {
            text = "200";
        }
        return text;

    }

    private void openDialog(String a) {

        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog_view);
        Button dialogBtn = dialog.findViewById(R.id.collectBtn_dialog);
        TextView textView = dialog.findViewById(R.id.pointTv_dialog);
        textView.setText("You Have Earned: "+a+" Points");
        dialog.show();
        dialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinCount++;
                spinCountTv.setText(spinCount+"/"+spin_limit);
                score=score+Integer.parseInt(a);
                pointsTv.setText(String.valueOf(score));

                reference.child(universal_uid).child("my_token").setValue(myUid);
                ref_user.child(myUid).child("temp_score").setValue(String.valueOf(score));

                flag=0;
                dialog.dismiss();
            }
        });

    }

    private void play2(int deg2) {

        degree_old = degree%360;
        //deg2 =r.nextInt(3600)+720;
        RotateAnimation rotateAnimation = new RotateAnimation(degree_old,deg2,
                RotateAnimation.RELATIVE_TO_SELF,0.5f,RotateAnimation.RELATIVE_TO_SELF,0.5f);
        rotateAnimation.setDuration(3600);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setInterpolator(new DecelerateInterpolator());

        MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.soundspinner);

        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
               // mediaPlayer.start();
                //pointsTv.setText("Waiting");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
               // mediaPlayer.stop();
                // openDialog(currentNumber(360-(degree%360)));
                //  pointsTv.setText(currentNumber(360-(degree%360)));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        spinView.startAnimation(rotateAnimation);

    }

    @Override
    protected void onStart() {
        super.onStart();
        ref_user.child(myUid).child("status").setValue("1");
    }

    @Override
    protected void onPause() {
        super.onPause();
        ref_user.child(myUid).child("status").setValue("0");
    }

    @Override
    protected void onResume() {
        super.onResume();
        ref_user.child(myUid).child("status").setValue("1");
    }

    @Override
    protected void onStop() {
        super.onStop();
        ref_user.child(myUid).child("status").setValue("0");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ref_user.child(myUid).child("status").setValue("0");
        reference.child(inviter_uid).child("left").setValue("1");

    }

}
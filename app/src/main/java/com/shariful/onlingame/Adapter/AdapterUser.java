package com.shariful.onlingame.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shariful.onlingame.Model.UserOnline;
import com.shariful.onlingame.R;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.Myholder> {
    String myUid;
    String myName;
    Context context;
    List<UserOnline> userList;

    public AdapterUser(Context context, List<UserOnline> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_row,parent,false);
        return new Myholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder, int position) {
              getMyName();
             String name = userList.get(position).getName();
             String player2_uid = userList.get(position).getUid();
             holder.nameTv.setText(name);

             holder.inviteBtn.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     DatabaseReference ref = FirebaseDatabase.getInstance().getReference("invite_list");

                     ref.child(myUid).child("inviter_uid").setValue(myUid);
                     ref.child(myUid).child("inviter_name").setValue(myName);
                     ref.child(myUid).child("player_name").setValue(name);
                     ref.child(myUid).child("player_uid").setValue(player2_uid);
                     ref.child(myUid).child("acceptation").setValue("no");
                 }
             });
    }

    private void getMyName() {
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("userList").child(myUid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    myName = snapshot.child("name").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class Myholder extends RecyclerView.ViewHolder {
        TextView nameTv;
        Button inviteBtn;
        public Myholder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.nameTv_row);
            inviteBtn = itemView.findViewById(R.id.inviteBtn_row);

        }
    }
}

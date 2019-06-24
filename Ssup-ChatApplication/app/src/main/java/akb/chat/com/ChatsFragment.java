package akb.chat.com;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View chatsView;
    private RecyclerView recyclerView;
    private DatabaseReference chatRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentuserid;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance();
        currentuserid = mAuth.getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentuserid);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        // Inflate the layout for this fragment
        chatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = (RecyclerView) chatsView.findViewById(R.id.chats_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return chatsView;
    }

    @Override
    public void onStart() {

        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRef, Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                final String uids = getRef(position).getKey();
                final String[] retImage = {"default_image"};


                UsersRef.child(uids).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            if (dataSnapshot.hasChild("image")) {
                                retImage[0] = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image).into(holder.dp);
                            }

                            final String retName = dataSnapshot.child("name").getValue().toString();
                            final String retStatus = dataSnapshot.child("status").getValue().toString();

                            holder.name.setText(retName);
                            holder.status.setText("Last seen");

                            if (dataSnapshot.child("user_state").hasChild("state")) {
                                String state = dataSnapshot.child("user_state").child("state").getValue().toString();
                                String date = dataSnapshot.child("user_state").child("date").getValue().toString();
                                String time = dataSnapshot.child("user_state").child("time").getValue().toString();

                                if (state.equals("online")) {
                                    holder.status.setText("Online");
                                }
                                else if (state.equals("offline")) {
                                    holder.status.setText("Last seen: " + date + " at " + time);
                                }
                            }
                            else {
                                holder.status.setText("Offline");
                            }

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(getContext(), ChatActivity.class);
                                    i.putExtra("uid", uids);
                                    i.putExtra("uname", retName);
                                    i.putExtra("uimage", retImage[0]);
                                    startActivity(i);
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                return new ChatsViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        CircleImageView dp;
        TextView name, status;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            dp = itemView.findViewById(R.id.users_profile_img);
            name = itemView.findViewById(R.id.user_profile_name);
            status = itemView.findViewById(R.id.user_profile_status);
        }
    }

}

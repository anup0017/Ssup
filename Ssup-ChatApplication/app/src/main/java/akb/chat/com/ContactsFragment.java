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
import android.widget.ImageView;
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
public class ContactsFragment extends Fragment {

    private View ContactsView;
    private RecyclerView recyclerView;
    private DatabaseReference contactsRef, usersRef;
    private FirebaseAuth mAuth;
    private String current_uid;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance();
        current_uid = mAuth.getCurrentUser().getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(current_uid);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);


        recyclerView = (RecyclerView) ContactsView.findViewById(R.id.contacts_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(contactsRef, Contacts.class)
                        .build();

        final FirebaseRecyclerAdapter<Contacts, contactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, contactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final contactsViewHolder holder, final int position, @NonNull Contacts model) {
                String usersIds = getRef(position).getKey();
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visitUserId = getRef(position).getKey();
                        Intent i = new Intent(getContext(), ProfileActivity.class);
                        i.putExtra("uID", visitUserId);
                        startActivity(i);
                    }
                });

                usersRef.child(usersIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            if (dataSnapshot.child("user_state").hasChild("state")) {
                                String state = dataSnapshot.child("user_state").child("state").getValue().toString();
                                String date = dataSnapshot.child("user_state").child("date").getValue().toString();
                                String time = dataSnapshot.child("user_state").child("time").getValue().toString();

                                if (state.equals("online")) {
                                    holder.onlineIcon.setVisibility(View.VISIBLE);
                                }
                                else if (state.equals("offline")) {
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);
                                }
                            }
                            else {
                                holder.onlineIcon.setVisibility(View.INVISIBLE);
                            }

                            if (dataSnapshot.hasChild("image")) {
                                String contactsdp = dataSnapshot.child("image").getValue().toString();
                                String contactsname = dataSnapshot.child("name").getValue().toString();
                                String contactsstatus = dataSnapshot.child("status").getValue().toString();

                                holder.username.setText(contactsname);
                                holder.userstatus.setText(contactsstatus);

                                Picasso.get().load(contactsdp).placeholder(R.drawable.profile_image).into(holder.profiledp);
                            }
                            else {
                                String contactsname = dataSnapshot.child("name").getValue().toString();
                                String contactsstatus = dataSnapshot.child("status").getValue().toString();

                                holder.username.setText(contactsname);
                                holder.userstatus.setText(contactsstatus);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public contactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                contactsViewHolder viewHolder = new contactsViewHolder(view);
                return viewHolder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class contactsViewHolder extends RecyclerView.ViewHolder {
        TextView username, userstatus;
        CircleImageView profiledp;
        ImageView onlineIcon;
        public contactsViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.user_profile_name);
            userstatus = itemView.findViewById(R.id.user_profile_status);
            profiledp = itemView.findViewById(R.id.users_profile_img);
            onlineIcon = (ImageView) itemView.findViewById(R.id.online);
        }
    }

}

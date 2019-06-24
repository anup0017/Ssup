package akb.chat.com;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String recUserId, currentState, senderUserId, dpUrl = null, name;
    private TextView visitName, visitStatus;
    private Button sendMessage, decline_request;
    private CircleImageView visitDp;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference, chatRef, contactsRef, NotifRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        currentState = "new";

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotifRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        recUserId = getIntent().getExtras().get("uID").toString();
        senderUserId = mAuth.getUid();

        visitName = (TextView) findViewById(R.id.visit_user_name);
        sendMessage = (Button) findViewById(R.id.send_message);
        visitStatus = (TextView) findViewById(R.id.visit_user_status);
        visitDp = (CircleImageView) findViewById(R.id.visit_dp);
        decline_request = (Button) findViewById(R.id.decline_request);

        RetrieveUserInfo();

        visitDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dpUrl != null) {
                    Intent i = new Intent(ProfileActivity.this, ImageViewerActivity.class);
                    i.putExtra("url", dpUrl);
                    startActivity(i);
                }
                else
                {
                    Toast.makeText(ProfileActivity.this, name + " has not set a profile picture.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void RetrieveUserInfo() {
        databaseReference.child(recUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && dataSnapshot.hasChild("image")) {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(visitDp);
                    visitName.setText(userName);
                    visitStatus.setText(userStatus);
                    dpUrl = userImage;

                    ManageChatRequest();
                }
                else
                {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    visitName.setText(userName);
                    visitStatus.setText(userStatus);
                    name= userName;

                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequest() {

        chatRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(recUserId)) {
                    String requestType = dataSnapshot.child(recUserId).child("request_type").getValue().toString();

                    if (requestType.equals("sent")) {
                        currentState = "request_sent";
                        sendMessage.setText("Cancel Chat Request");
                    }
                    else if (requestType.equals("received")) {
                        currentState = "request_received";
                        sendMessage.setText("Accept Chat Request");

                        decline_request.setVisibility(View.VISIBLE);
                        decline_request.setEnabled(true);

                        decline_request.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelChatRequest();
                            }
                        });
                    }
                }
                else {
                    contactsRef.child(senderUserId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(recUserId)) {
                                        currentState = "friends";
                                        sendMessage.setText("Remove this contact");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (!senderUserId.equals(recUserId)) {
            sendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage.setEnabled(false);

                    if (currentState.equals("new")) {
                        sendChatRequest();
                    }

                    if (currentState.equals("request_sent")){
                        CancelChatRequest();
                    }

                    if (currentState.equals("request_received")){
                        AcceptChatRequest();
                    }
                    if (currentState.equals("friends")) {
                        RemoveSpecificContacts();
                    }

                }
            });
        }
        else {
            sendMessage.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpecificContacts() {
        contactsRef.child(senderUserId).child(recUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    contactsRef.child(recUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                sendMessage.setEnabled(true);
                                currentState = "new";
                                sendMessage.setText("Send Chat Request");
                                decline_request.setVisibility(View.INVISIBLE);
                                decline_request.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void AcceptChatRequest() {
        contactsRef.child(senderUserId).child(recUserId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            contactsRef.child(recUserId).child(senderUserId)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                chatRef.child(senderUserId).child(recUserId).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                chatRef.child(recUserId).child(senderUserId).removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                sendMessage.setEnabled(true);
                                                                                currentState = "friends";
                                                                                sendMessage.setText("Remove this contact");
                                                                                decline_request.setVisibility(View.INVISIBLE);
                                                                                decline_request.setEnabled(false);
                                                                            }
                                                                        });
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelChatRequest() {
        chatRef.child(senderUserId).child(recUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    chatRef.child(recUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                sendMessage.setEnabled(true);
                                currentState = "new";
                                sendMessage.setText("Send Chat Request");
                                decline_request.setVisibility(View.INVISIBLE);
                                decline_request.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendChatRequest() {
        chatRef.child(senderUserId).child(recUserId).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRef.child(recUserId).child(senderUserId).child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                HashMap<String, String> notificationMap = new HashMap<>();
                                                notificationMap.put("from", senderUserId);
                                                notificationMap.put("type", "request");

                                                NotifRef.child(recUserId).push().setValue(notificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    sendMessage.setEnabled(true);
                                                                    currentState = "request_sent";
                                                                    sendMessage.setText("Cancel Chat Request");
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}

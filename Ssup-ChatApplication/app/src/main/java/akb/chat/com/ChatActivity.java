package akb.chat.com;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private EditText input;
    private ImageView send, sendFiles;
    private String msgRecId, msgSenderId, msgRecName, msgRecDp;
    private TextView name, lastseen;
    private CircleImageView dp;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private String saveTime, saveDate;
    private String checker = "", myUrl = "";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        input = (EditText) findViewById(R.id.userInput);
        send = (ImageView) findViewById(R.id.send);
        sendFiles = (ImageView) findViewById(R.id.send_files);


        msgRecName = getIntent().getExtras().get("uname").toString();
        msgRecId = getIntent().getExtras().get("uid").toString();
        msgRecDp = getIntent().getExtras().get("uimage").toString();

        RootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        msgSenderId = mAuth.getCurrentUser().getUid();

        InitializeControlles();
        displayLastSeen();
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        name.setText(msgRecName);
        Picasso.get().load(msgRecDp).placeholder(R.drawable.profile_image).into(dp);

        sendFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[] {
                        "Images",
                        "PDF files",
                        "DOC files"
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Choose the file type");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            checker = "image";
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_GET_CONTENT);
                            i.setType("image/*");
                            startActivityForResult(i.createChooser(i, "Select Image"), 438);
                        }
                        if (which == 1) {
                            checker = "pdf";

                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_GET_CONTENT);
                            i.setType("application/pdf");
                            startActivityForResult(i.createChooser(i, "Select PDF file"), 438);
                        }
                        if(which == 2) {
                            checker = "docx";

                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_GET_CONTENT);
                            i.setType("application/msword");
                            startActivityForResult(i.createChooser(i, "Select Word file"), 438);
                        }
                    }
                });
                builder.show();
            }
        });

        //getdata of firebase from onStart to onCreate

        RootRef.child("Messages").child(msgSenderId).child(msgRecId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData()!=null) {

            progressDialog.setTitle("Sending file");
            progressDialog.setMessage("Sending, Please wait...");
            //progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            fileUri = data.getData();

            if (!checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                final String msgSenderRef = "Messages/" + msgSenderId + "/" + msgRecId;
                final String msgRecRef = "Messages/" + msgRecId + "/" + msgSenderId;

                DatabaseReference UserKeyRef = RootRef.child("Messages").child(msgSenderId).child(msgRecId).push();

                final String msgPushId = UserKeyRef.getKey();

                final StorageReference filePath = storageReference.child(msgPushId + "." + checker);

                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                progressDialog.dismiss();
                                //Toast.makeText(SettingsActivity.this, "onSuccess: uri= " + uri.toString(), Toast.LENGTH_SHORT).show();
                                final String url = uri.toString();

                                Map messageImageBody = new HashMap();
                                messageImageBody.put("message", url);
                                messageImageBody.put("name", fileUri.getLastPathSegment());
                                messageImageBody.put("type", checker);
                                messageImageBody.put("from", msgSenderId);
                                messageImageBody.put("to", msgRecId);
                                messageImageBody.put("messageID", msgPushId);
                                messageImageBody.put("time", saveTime);
                                messageImageBody.put("date", saveDate);

                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(msgSenderRef + "/" + msgPushId, messageImageBody);
                                messageBodyDetails.put( msgRecRef + "/" + msgPushId, messageImageBody);

                                RootRef.updateChildren(messageBodyDetails);
                                progressDialog.dismiss();

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                        progressDialog.setMessage((int) p + "% uploading...");
                    }
                });

            }
            else if (checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String msgSenderRef = "Messages/" + msgSenderId + "/" + msgRecId;
                final String msgRecRef = "Messages/" + msgRecId + "/" + msgSenderId;

                DatabaseReference UserKeyRef = RootRef.child("Messages").child(msgSenderId).child(msgRecId).push();

                final String msgPushId = UserKeyRef.getKey();

                final StorageReference filePath = storageReference.child(msgPushId + "." + "jpg");

                uploadTask = filePath.putFile(fileUri); //fileuri stores the image
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            Map messageImageBody = new HashMap();
                            messageImageBody.put("message", myUrl);
                            messageImageBody.put("name", fileUri.getLastPathSegment());
                            messageImageBody.put("type", checker);
                            messageImageBody.put("from", msgSenderId);
                            messageImageBody.put("to", msgRecId);
                            messageImageBody.put("messageID", msgPushId);
                            messageImageBody.put("time", saveTime);
                            messageImageBody.put("date", saveDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(msgSenderRef + "/" + msgPushId, messageImageBody);
                            messageBodyDetails.put( msgRecRef + "/" + msgPushId, messageImageBody);


                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        progressDialog.dismiss();
                                        Toast.makeText(ChatActivity.this, "Image Sent", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        progressDialog.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        }
                    }
                });
            }
            else {
                progressDialog.dismiss();
                Toast.makeText(this, "Nothing selected.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void InitializeControlles() {

        toolbar = (Toolbar) findViewById(R.id.chat_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(view);

        name = (TextView) findViewById(R.id.custom_name);
        dp = (CircleImageView) findViewById(R.id.custom_dp);
        lastseen = (TextView) findViewById(R.id.custom_last_seen);

        progressDialog = new ProgressDialog(ChatActivity.this);

        messageAdapter = new MessageAdapter(messagesList);

        recyclerView = (RecyclerView) findViewById(R.id.chat_recycler);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messageAdapter);

        Calendar calendar =  Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd MMM, yyyy");
        saveDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        saveTime = currentTimeFormat.format(calendar.getTime());

    }



    @Override
    protected void onStart() {
        super.onStart();
    }



    private void sendMessage(){
        String msg = input.getText().toString();
        input.setText("");

        if (TextUtils.isEmpty(msg)) {
            Toast.makeText(this, "Text field cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String msgSenderRef = "Messages/" + msgSenderId + "/" + msgRecId;
            String msgRecRef = "Messages/" + msgRecId + "/" + msgSenderId;

            DatabaseReference UserKeyRef = RootRef.child("Messages").child(msgSenderId).child(msgRecId).push();

            String msgPushId = UserKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", msg);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", msgSenderId);
            messageTextBody.put("to", msgRecId);
            messageTextBody.put("messageID", msgPushId);
            messageTextBody.put("time", saveTime);
            messageTextBody.put("date", saveDate);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(msgSenderRef + "/" + msgPushId, messageTextBody);
            messageBodyDetails.put( msgRecRef + "/" + msgPushId, messageTextBody);


            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if (task.isSuccessful())
                    { }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Message not sent", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }

    private void displayLastSeen() {
        RootRef.child("Users").child(msgRecId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("user_state").hasChild("state")) {
                    String state = dataSnapshot.child("user_state").child("state").getValue().toString();
                    String date = dataSnapshot.child("user_state").child("date").getValue().toString();
                    String time = dataSnapshot.child("user_state").child("time").getValue().toString();

                    if (state.equals("online")) {
                        lastseen.setText("Online");
                    }
                    else if (state.equals("offline")) {
                        lastseen.setText("Last seen: " + date + " at " + time);
                    }
                }
                else {
                    lastseen.setText("Offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

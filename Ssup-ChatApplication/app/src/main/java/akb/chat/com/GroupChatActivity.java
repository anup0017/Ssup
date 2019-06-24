package akb.chat.com;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView sendMessage;
    private EditText userMessageInput;
    private ScrollView scrollView;
    private TextView displayTextMessage;

    private String currentGroupName, currentUID, currentUserName, currentDate, currentTime;

    private FirebaseAuth mAuth;
    private DatabaseReference userReference, groupReference, groupMessageKeyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUID = mAuth.getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        groupReference = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        InitializeFields();

        GetUserInfo();

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageToDatabase();
                userMessageInput.setText("");
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        //getdata from firebase onstart to oncreate

        groupReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    DisplayMessages(dataSnapshot);
                }
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
    protected void onStart() {
        super.onStart();

    }

    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext()) {
            String ChatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String ChatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String ChatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String ChatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessage.append("> " + ChatName + " :\n> " +
                    ChatMessage + "\n> (" +
                    ChatDate + ", " +
                    ChatTime + ")\n\n\n");
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    private void SaveMessageToDatabase() {
        String message = userMessageInput.getText().toString();
        String messageKey = groupReference.push().getKey();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Text field cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calForDate =  Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd MMM, yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime =  Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupReference.updateChildren(groupMessageKey);

            groupMessageKeyRef = groupReference.child(messageKey);

            HashMap<String, Object> groupMessageInfo = new HashMap<>();
            groupMessageInfo.put("name", currentUserName);
            groupMessageInfo.put("message", message);
            groupMessageInfo.put("date", currentDate);
            groupMessageInfo.put("time", currentTime);

            groupMessageKeyRef.updateChildren(groupMessageInfo);
        }
    }

    private void GetUserInfo() {
        userReference.child(currentUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void InitializeFields() {
        sendMessage = (ImageView) findViewById(R.id.send_button);
        userMessageInput  = (EditText) findViewById(R.id.input_group_message);
        displayTextMessage = (TextView) findViewById(R.id.group_chat_text_display);

        toolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(currentGroupName);

        scrollView = (ScrollView) findViewById(R.id.my_scroll_view);

    }
}

package akb.chat.com;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private TabsAccessorAdapter tabsAccessorAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    String name, currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        viewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        tabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsAccessorAdapter);

        tabLayout = (TabLayout) findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            sendToLoginPage();
        }
        else {

            updateUserStatus("online");

            VerifyUserExistence();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            updateUserStatus("online");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistence() {
        String currentUID = mAuth.getCurrentUser().getUid();
        databaseReference.child("Users").child(currentUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("name").exists())) {

                    name = dataSnapshot.child("name").getValue().toString();
                    mToolBar = (Toolbar) findViewById(R.id.main_page_toolbar);
                    setSupportActionBar(mToolBar);
                    getSupportActionBar().setTitle("Ssup " + name + "!");
                    //Toast.makeText(MainActivity.this, name, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Intent i = new Intent(MainActivity.this, RegisterProfileActivity.class);
                    startActivity(i);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendToLoginPage() {
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_logout_option) {
            AlertDialog.Builder builder
                    = new AlertDialog
                    .Builder(MainActivity.this);

            builder.setMessage("Are you sure you want to logout?");
            builder.setTitle("Alert !");
            builder.setCancelable(false);

            builder.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which)
                        {
                            updateUserStatus("offline");
                            mAuth.signOut();
                            sendToLoginPage();
                        }
                    });

            builder.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which)
                        {
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = builder.create();

            alertDialog.show();

        }

        if (item.getItemId() == R.id.main_create_group_option) {
            RequestNewGroup();
        }
        if (item.getItemId() == R.id.main_settings_option) {
            sendToSettingsPage();
        }

        if (item.getItemId() == R.id.main_new_friends_option) {
            sendToFindFriendsPage();
        }
        if (item.getItemId() == R.id.main_settings_option) {
            sendToSettingsPage();
        }
        return true;
    }

    private void sendToFindFriendsPage() {
        Intent i = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(i);
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name : ");

        final EditText groupName = new EditText(MainActivity.this);
        groupName.setHint("eg. Study Buddies");
        builder.setView(groupName);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupname = groupName.getText().toString();
                if (TextUtils.isEmpty(groupname)) {
                    Toast.makeText(MainActivity.this, "Group name can't be empty...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    CreateNewGroup(groupname);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void CreateNewGroup(final String groupname) {
        databaseReference.child("Groups").child(groupname).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, groupname + " group is created successfully!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendToSettingsPage() {
        Intent i = new Intent(MainActivity.this, SettingsActivity.class);
        //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        //finish();
    }

    private void updateUserStatus(String state) {
        String saveTime, saveDate;

        Calendar calendar =  Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd MMM, yyyy");
        saveDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        saveTime = currentTimeFormat.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveTime);
        onlineStateMap.put("date", saveDate);
        onlineStateMap.put("state", state);

        currentUserId = mAuth.getCurrentUser().getUid();

        databaseReference.child("Users").child(currentUserId).child("user_state").updateChildren(onlineStateMap);
    }

}

package akb.chat.com;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Button login, phoneLogin;
    private EditText log_email, log_pass;
    private TextView goToReg, forgot_pass;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        InitializeFields();

        goToReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToRegisterPage();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserLogin();
            }
        });

        phoneLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, PhoneLoginActivity.class);
                startActivity(i);
            }
        });

    }

    private void AllowUserLogin() {
        String email = log_email.getText().toString();
        String pass = log_pass.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter Email...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Please enter Password..", Toast.LENGTH_SHORT).show();
        }
        else {
            progressDialog.setTitle("Sign in");
            progressDialog.setMessage("Please wait...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                        String currentUserId =  mAuth.getCurrentUser().getUid();
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                        UserRef.child(currentUserId).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    progressDialog.dismiss();
                                    sendToMainPage();
                                    Toast.makeText(LoginActivity.this, "Login Success!!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else {
                        progressDialog.dismiss();
                        String message = task.getException().toString();
                        Toast.makeText(LoginActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void sendToRegisterPage() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void InitializeFields() {

        login = (Button) findViewById(R.id.login_button);
        phoneLogin = (Button) findViewById(R.id.phone_login);
        log_email = (EditText) findViewById(R.id.login_email);
        log_pass = (EditText) findViewById(R.id.login_password);
        goToReg = (TextView) findViewById(R.id.new_account);
        forgot_pass = (TextView) findViewById(R.id.forgot_password);

        progressDialog = new ProgressDialog(this);

    }

    private void sendToMainPage() {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

}

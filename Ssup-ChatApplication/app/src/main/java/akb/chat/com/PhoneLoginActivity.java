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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVerCode, submit;
    private EditText phoneInput, verCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        InitializeField();
        progressDialog = new ProgressDialog(this);

        sendVerCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneInput.getText().toString();
                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(PhoneLoginActivity.this, "Phone number field cannot be empty...", Toast.LENGTH_SHORT).show();
                }
                else {
                    progressDialog.setTitle("Phone Verification");
                    progressDialog.setMessage("Please wait while we authenticate your phone number");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerCode.setVisibility(View.INVISIBLE);
                phoneInput.setVisibility(View.INVISIBLE);

                String vercode = verCode.getText().toString();

                if (TextUtils.isEmpty(vercode)) {
                    Toast.makeText(PhoneLoginActivity.this, "Enter the verification code first", Toast.LENGTH_SHORT).show();
                }
                else {
                    progressDialog.setTitle("Verification Code");
                    progressDialog.setMessage("Please wait while we verify your code");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, vercode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                progressDialog.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone number or verification code...", Toast.LENGTH_SHORT).show();
                sendVerCode.setVisibility(View.VISIBLE);
                phoneInput.setVisibility(View.VISIBLE);

                submit.setVisibility(View.INVISIBLE);
                verCode.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;

                progressDialog.dismiss();

                Toast.makeText(PhoneLoginActivity.this, "Verification code sent.", Toast.LENGTH_SHORT).show();

                sendVerCode.setVisibility(View.INVISIBLE);
                phoneInput.setVisibility(View.INVISIBLE);

                submit.setVisibility(View.VISIBLE);
                verCode.setVisibility(View.VISIBLE);
            }

        };
    }



    private void InitializeField() {
        sendVerCode = (Button) findViewById(R.id.send_ver_code);
        submit = (Button) findViewById(R.id.verify_button);
        phoneInput = (EditText) findViewById(R.id.phone_input);
        verCode = (EditText) findViewById(R.id.ver_code);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String current_uid = mAuth.getCurrentUser().getUid();
                            String device_token = FirebaseInstanceId.getInstance().getToken();

                            UsersRef.child(current_uid).child("device_token").setValue(device_token).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        Toast.makeText(PhoneLoginActivity.this, "Login Success!!", Toast.LENGTH_SHORT).show();
                                        sendUserToMainPage();
                                    }
                                    else
                                    {
                                        Toast.makeText(PhoneLoginActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                String message = task.getException().toString();
                                Toast.makeText(PhoneLoginActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    private void sendUserToMainPage() {
        Intent i = new Intent(PhoneLoginActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}

package com.example.stegnography;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
	private Button buttonRegistor;
	private EditText editTextMail,editTextpass;
	private TextView textViewSignin;
	
	private ProgressDialog progressDialog;
	
	private FirebaseAuth fireBaseAuth;
	private String UserId;
	private final FirebaseFirestore fstore = FirebaseFirestore.getInstance();
	private String password;
	private String email;
	private DocumentReference dref;
	private final String count="0";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		fireBaseAuth = FirebaseAuth.getInstance();
		
		if (fireBaseAuth.getCurrentUser()!=null){
			finish();
			startActivity(new Intent(getApplicationContext(),Home.class));
		}
		
		buttonRegistor =  findViewById(R.id.buttonRegister);
		editTextMail =  findViewById(R.id.edittextemail);
		editTextpass =  findViewById(R.id.edittextpass);
		textViewSignin =  findViewById(R.id.textviewsignin);
		
		progressDialog = new ProgressDialog(this);
		buttonRegistor.setOnClickListener((this));
		textViewSignin.setOnClickListener((this));
	}
	private void registerUser(){
		password = editTextpass.getText().toString().trim();
		email = editTextMail.getText().toString().trim();
		if(TextUtils.isEmpty(email)){
			Toast.makeText(this,"Please Enter email",Toast.LENGTH_SHORT).show();
			return;
		}
		if(TextUtils.isEmpty(password)){
			Toast.makeText(this,"Please Enter email",Toast.LENGTH_SHORT).show();
			return;
		}
		progressDialog.setMessage("Registering User");
		progressDialog.show();
		fireBaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this,new OnCompleteListener<AuthResult>(){
			@Override
			public void onComplete(@NonNull Task<AuthResult> task){
				if(task.isSuccessful()){
					UserId = Objects.requireNonNull(fireBaseAuth.getCurrentUser()).getUid();
					dref = fstore.collection("User").document("UserId")
							.collection(UserId).document("Details");
					Map<String,Object> note = new HashMap<>();
					note.put("Email",email);
					note.put("Password",password);
					note.put("Count",count);
					dref.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
						@Override
						public void onSuccess(Void aVoid) {
							startActivity(new Intent(getApplicationContext(),UserDetails.class));
						}
					});
				}else{
					Toast.makeText(MainActivity.this,"Could not Register",Toast.LENGTH_SHORT).show();
				}
				progressDialog.dismiss();
			}
		});
	}
	
	
	@Override
	public void onClick(View v) {
		ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
				connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
			if(v==buttonRegistor){
				registerUser();
			}else if(v==textViewSignin){
				startActivity(new Intent(this,Login.class));
			}
		}
		else{
			Toast.makeText(MainActivity.this,"No Internet Connection",Toast.LENGTH_SHORT).show();
		}
	
	}
}
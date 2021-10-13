package com.example.stegnography;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.ayush.imagesteganographylibrary.Text.AsyncTaskCallback.TextDecodingCallback;
import com.ayush.imagesteganographylibrary.Text.ImageSteganography;
import com.ayush.imagesteganographylibrary.Text.TextDecoding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

public class Decrypt extends AppCompatActivity implements TextDecodingCallback {
	
	private static final int SELECT_PICTURE = 100;
	private static final String TAG = "Decode Class";	
	//Initializing the UI components
	private TextView textView;
	private ImageView imageView;
	private EditText message,editTextfname,editTextlname,editTextdob,editTextgender,editTextemail
			,editTextaddress,editTextnumber;
	private EditText secret_key;
	private String fname,lname,number,address,dob,gender="",UserId,name,email,imageurl,fileName,
			Email="Email",FirstName="First Name",LastName="Last Name",PhoneNumber="Phone " +
			"Number",Address="Address",DateOfBirth="Date Of Birth",eMessage;
	private FirebaseFirestore db = FirebaseFirestore.getInstance();
	private FirebaseStorage storage = FirebaseStorage.getInstance();
	private StorageReference storageRef;
	private DocumentReference dref;
	private DatabaseReference databaseReference;
	
	//Bitmap
	private Bitmap original_image;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_decrypt);
	setContentView(R.layout.activity_decrypt);
		String database_Path = "All_Encoded_Images";
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		databaseReference= database.getReference(database_Path);
		FirebaseAuth firebaseAuth2 = FirebaseAuth.getInstance();
		if (firebaseAuth2.getCurrentUser()==null){
			finish();
			startActivity(new Intent(getApplicationContext(),Login.class));
		}
		UserId= firebaseAuth2.getCurrentUser().getUid();
		dref = db.collection("User").document("UserId")
				.collection(UserId).document("Details");
		dref.get().addOnSuccessListener((DocumentSnapshot documentSnapshot) -> {
			if (documentSnapshot.exists()) {
				email = documentSnapshot.getString(Email);
				fname = documentSnapshot.getString(FirstName);
				lname = documentSnapshot.getString(LastName);
				number = documentSnapshot.getString(PhoneNumber);
				address = documentSnapshot.getString(Address);
				dob = documentSnapshot.getString(DateOfBirth);
				name=fname+lname;
			}
		});

        //Instantiation of UI components
        textView = findViewById(R.id.whether_decoded);
        imageView = findViewById(R.id.imageview);
        message = findViewById(R.id.editTextaddress);
        editTextfname=findViewById(R.id.editTextTextFirstName);
        editTextlname=findViewById(R.id.editTextTextLastName);
		editTextdob=findViewById(R.id.editTextDate);
		editTextemail=findViewById(R.id.editTextTextEmailAddress);
		editTextnumber=findViewById(R.id.editTextPhone);
		editTextgender=findViewById(R.id.editTextGender);
        secret_key = findViewById(R.id.secret_key);

        Button choose_image_button = findViewById(R.id.choose_image_button);
        Button decode_button = findViewById(R.id.decode_button);
		
		choose_image_button.setOnClickListener((View view) -> getImages());

        //Decode Button
        decode_button.setOnClickListener(view -> {
            if (fileName != null) {
	            Log.d(TAG,"filename present");
	            ImageSteganography imageSteganography = new ImageSteganography(secret_key.getText().toString(),
			            original_image);
	            //Making the TextDecoding object
	            TextDecoding textDecoding = new TextDecoding(Decrypt.this, Decrypt.this);
	            //Execute Task
	            textDecoding.execute(imageSteganography);
            }else{
	            Log.d(TAG,"filename not present");
            }
        });
    }
	
    private void getImages() {
	    mGetContent.launch("image/*");
	    
    }
	
	private Uri mImageUri;
	ActivityResultLauncher<String> mGetContent = registerForActivityResult(
			    new ActivityResultContracts.GetContent(),
			    result -> {
				    if(result!=null){
					    mImageUri=result;
					    imageView.setImageURI(mImageUri);
					    fileName=getFileName(mImageUri);
					    try {
						    original_image=
								    MediaStore.Images.Media.getBitmap(this.getContentResolver(),
										    mImageUri);
					    } catch (IOException e) {
						    e.printStackTrace();
					    }
				    }
			    });
	
	private String getFileName(Uri mImageUri) {
		Cursor c = getContentResolver().query(mImageUri, null, null, null, null);
		c.moveToFirst();
		return c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
	}
	
	@Override
	public void onStartTextEncoding() {
	
	}
	
	@Override
	public void onCompleteTextEncoding(ImageSteganography result) {
		if (result != null) {
			if (!result.isDecoded())
				textView.setText("No message found");
			else {
				if (!result.isSecretKeyWrong()) {
					textView.setText("Decoded");
					String original= result.getMessage();
					String exp = original.substring(1,original.length()-1);
					String[] names;
					names = exp.split(",");
					String number="Phone Number=";
					String fname="First Name=";
					String lname="Last Name=";
					String email="Email=";
					String gender="Gender=";
					String dob="Date Of Birth=";
					String address="Address=";
					
					for (String s : names) {
						if (s.contains(number)) {
							int testindex = s.indexOf('=');
							String test = s.substring(testindex + 1);
							editTextnumber.setText(test);
						} else if (s.contains(dob)) {
							int testindex = s.indexOf('=');
							String test = s.substring(testindex + 1);
							editTextdob.setText(test);
						} else if (s.contains(fname)) {
							int testindex = s.indexOf('=');
							String test = s.substring(testindex + 1);
							editTextfname.setText(test);
						} else if (s.contains(lname)) {
							int testindex = s.indexOf('=');
							String test = s.substring(testindex + 1);
							editTextlname.setText(test);
						} else if (s.contains(email)) {
							int testindex = s.indexOf('=');
							String test = s.substring(testindex + 1);
							editTextemail.setText(test);
						} else if (s.contains(gender)) {
							int testindex = s.indexOf('=');
							String test = s.substring(testindex + 1);
							editTextgender.setText(test);
						} else if (s.contains(address)) {
							int testindex = s.indexOf('=');
							String test = s.substring(testindex + 1);
							message.setText(test);
						}
					}
				} else {
					textView.setText("Wrong secret key");
				}
			}
		} else {
			textView.setText("Select Image First");
		}
		
	}
}

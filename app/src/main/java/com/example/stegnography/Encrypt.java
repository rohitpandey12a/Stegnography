package com.example.stegnography;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ayush.imagesteganographylibrary.Text.AsyncTaskCallback.TextEncodingCallback;
import com.ayush.imagesteganographylibrary.Text.ImageSteganography;
import com.ayush.imagesteganographylibrary.Text.TextEncoding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

public class Encrypt extends AppCompatActivity implements TextEncodingCallback {
    
    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "Encode Class";
    //Created variables for UI
    private TextView whether_encoded;
    private ImageView imageView;
    private EditText secret_key;
    //Bitmaps
    private Bitmap original_image;
    private Bitmap encoded_image;
    private String fname,lname,number,address,dob,gender="",UserId,name,email,imageurl,fileName,
            Email="Email",FirstName="First Name",LastName="Last Name",PhoneNumber="Phone " +
            "Number",Address="Address",DateOfBirth="Date Of Birth",Gender="Gender",eMessage;
    Dictionary message;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef;
    private DocumentReference dref;
    private TextEncoding textEncoding;
    private ImageSteganography imageSteganography;
    private DatabaseReference databaseReference;
    private TextView imagename;
    private ProgressDialog save;
    //ImageAdapter adapter;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_encrypt);
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
                gender=documentSnapshot.getString(Gender);
                name=fname+lname;
                message=new Hashtable();
                message.put(Email,email);
                message.put(FirstName,fname);
                message.put(LastName,lname);
                message.put(PhoneNumber,number);
                message.put(Address,address);
                message.put(DateOfBirth,dob);
                message.put(Gender,gender);
                Toast.makeText(Encrypt.this, "info available", Toast.LENGTH_SHORT).show();
            }else{
    
                Toast.makeText(Encrypt.this, "info not available", Toast.LENGTH_SHORT).show();
            }
            eMessage= String.valueOf(message);
            Log.d("EXPERIMENT",eMessage);
        });
        
                //"fname:"+fname+"/"+"lname:"+lname+"/"+"email:"+email+"/"+"number:"+number+"/"+"address:"+address+"/"+"dob:"+dob+"/"+"gender:"+gender;
        //initialized the UI components

        whether_encoded = findViewById(R.id.whether_encoded);
        imageView = findViewById(R.id.imageview);
        secret_key = findViewById(R.id.secret_key);
        imagename=findViewById(R.id.imageName);
        Button choose_image_button = findViewById(R.id.choose_image_button);
        Button encode_button = findViewById(R.id.encode_button);
        Button save_image_button = findViewById(R.id.save_image_button);
        Button get_images_button = findViewById(R.id.getImage);
        checkAndRequestPermissions();
        get_images_button.setOnClickListener(v -> getImages());
        //Choose image button
        choose_image_button.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(),DisplayImageActivity.class)));

        //Encode Button
        encode_button.setOnClickListener(view -> {
            try {
                original_image=((BitmapDrawable) imageView.getDrawable()).getBitmap();
            }catch (Exception e){
                Log.d(TAG, Objects.requireNonNull(e.getMessage()));
            }
            whether_encoded.setText("");
            if (fileName != null) {
                Log.d(TAG,"filename present"+fileName);
                if(eMessage.isEmpty()){
                    Log.d(TAG,"eMessage not present");
                    Toast.makeText(Encrypt.this, "Message not Available to encode",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.d(TAG,"eMessage present");
                    imageSteganography = new ImageSteganography(eMessage, secret_key.getText().toString(), original_image);
                    //TextEncoding object Instantiation
                    textEncoding = new TextEncoding(Encrypt.this, Encrypt.this);
                    //Executing the encoding
                    textEncoding.execute(imageSteganography);
                    Toast.makeText(Encrypt.this, "Encoded successfull", Toast.LENGTH_SHORT).show();
                }
            }else {
                Log.d(TAG,"Filename not present");
                Toast.makeText(Encrypt.this, "Encoding unsuccessfull", Toast.LENGTH_SHORT).show();
            }
        });

        //Save image button
        save_image_button.setOnClickListener(view -> {
            final Bitmap imgToSave = encoded_image;
            Thread PerformEncoding = new Thread(() -> saveToFireBaseStorage(imgToSave));
            save = new ProgressDialog(Encrypt.this);
            save.setMessage("Saving, Please Wait...");
            save.setTitle("Saving Image");
            save.setIndeterminate(false);
            save.setCancelable(false);
            save.show();
            PerformEncoding.start();
        });
    }
    
    private void getImages() {
        if(getIntent().hasExtra("URL")&&getIntent().hasExtra("TITLE")){
            imageurl= getIntent().getStringExtra("URL");
            fileName=getIntent().getStringExtra("TITLE");
            imagename.setText(fileName);
            Log.d(TAG,fileName);
            Glide.with(this).asBitmap().load(imageurl).into(imageView);
        }else {
            Toast.makeText(Encrypt.this, "Image not loaded", Toast.LENGTH_LONG).show();
        }
    }
    
    private void saveToFireBaseStorage(Bitmap imgToSave) {
        OutputStream fOut;
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "Encoded_" +fileName+ ".PNG"); // the File to
        // save ,
        if (file.exists()){
            int count=1;
            File filenew = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS),"Encoded_"+ count +fileName+".PNG");
            ++count;
            try {
                fOut = new FileOutputStream(filenew);
                imgToSave.compress(Bitmap.CompressFormat.PNG, 100, fOut); // saving the Bitmap to a file
                fOut.flush(); // Not really required
                fOut.close(); // do not forget to close the stream
                whether_encoded.post(() -> save.dismiss());
            } catch (IOException e) {
                e.printStackTrace();
            }
            startActivity(new Intent(getApplicationContext(),Home.class));
        }else {
            try {
                fOut = new FileOutputStream(file);
                imgToSave.compress(Bitmap.CompressFormat.PNG, 100, fOut); // saving the Bitmap to a file
                fOut.flush(); // Not really required
                fOut.close(); // do not forget to close the stream
                whether_encoded.post(() -> save.dismiss());
            } catch (IOException e) {
                e.printStackTrace();
            }
            startActivity(new Intent(getApplicationContext(),Home.class));
        }
    }
    
    private void checkAndRequestPermissions() {
        int permissionWriteStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int ReadPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (ReadPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), 1);
        }
    }
    
    @Override
    public void onStartTextEncoding() {
    
    }
    
    @Override
    public void onCompleteTextEncoding(ImageSteganography result) {
    
        if (result != null && result.isEncoded()) {
            encoded_image = result.getEncoded_image();
            whether_encoded.setText("Encoded");
            imageView.setImageBitmap(encoded_image);
        }
    }
}

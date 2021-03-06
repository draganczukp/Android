package com.example.pierwszyandek.contacts;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.example.pierwszyandek.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.util.Optional;
import java.util.concurrent.Executors;

public class AddContact extends AppCompatActivity {

    private ContactsViewModel contactsViewModel;

    private Contact current;

    private Uri selectedImage;

    private static final int GALLERY_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        TextView contactName = findViewById(R.id.contactName);
        TextView contactSurname = findViewById(R.id.contactSurname);
        TextView number = findViewById(R.id.contactNumber);
        TextView contactNote = findViewById(R.id.contactNote);
        FloatingActionButton addContact = findViewById(R.id.button);
        Switch genderPicker = findViewById(R.id.genderPicker);
        TextView gender = findViewById(R.id.gender);
        contactsViewModel = ViewModelProviders
                .of(this)
                .get(ContactsViewModel.class);

        if (getIntent().hasExtra("editing")) {
            ContactDao dao = ContactsDatabase.get(this).contactDao();

            int id = getIntent().getIntExtra("editing", -1);


            Executors.newSingleThreadExecutor().execute(() -> {
                current = dao.findById(id);
                contactName.setText(current.getContactName());
                contactSurname.setText(current.getContactSurname());
                number.setText(current.getNumber());
                contactNote.setText(current.getContactNote());
                genderPicker.setChecked(current.isGender());
                gender.setText(current.isGender() ? "Kobieta" : "Mezczyzna");

                selectedImage = Optional.ofNullable(current)
                        .map(Contact :: getAvatarPath)
                        .map(Uri :: parse)
                        .orElse(null);
                setAvatarImage(selectedImage);

            });
        } else {
            updateGenderInfo(false);
        }
    }

    public void onSaveButtonClick(View view) {
        TextView contactName = findViewById(R.id.contactName);
        TextView contactSurname = findViewById(R.id.contactSurname);
        TextView number = findViewById(R.id.contactNumber);
        TextView contactNote = findViewById(R.id.contactNote);
        Switch contactGenderSwitch = findViewById(R.id.genderPicker);

        String name = contactName.getText().toString();
        String surname = contactSurname.getText().toString();
        String number1 = number.getText().toString();
        String note = contactNote.getText().toString();

        String avatar = Optional.ofNullable(selectedImage).map(Uri::toString).orElse(null);

        Contact contact = Contact.builder()
                .contactName(name)
                .contactSurname(surname)
                .contactNote(note)
                .avatarPath(avatar)
                .gender(contactGenderSwitch.isChecked() ? false : true)
                .number(number1)
                .build();
        if (current != null) {
            contact.setId(current.getId());
            contactsViewModel.update(contact);
        } else {
            contactsViewModel.saveContact(contact);
            current = contact;
        }
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
    }
    private void updateGenderInfo(boolean femaleSelected) {

        boolean defaultAvatar = !Optional.ofNullable(current).map(Contact::getAvatarPath).isPresent();


        ImageButton avatarButton = findViewById(R.id.contactAvatar);

        if (femaleSelected) {


            if (defaultAvatar) {
                avatarButton.setImageResource(R.drawable.woman);
            }
        } else {

            if (defaultAvatar) {
                avatarButton.setImageResource(R.drawable.man);
            }
        }
    }
    private void setAvatarImage(Uri imagePath) {
        ImageButton avatarButton = findViewById(R.id.contactAvatar);

        Picasso.get()
                .load(imagePath)
                .resizeDimen(R.dimen.avatarDim, R.dimen.avatarDim)
                .centerCrop()
                .into(avatarButton);

    }

    public void pickFromGallery(View view) {


        //Create an Intent with action as ACTION_PICK
        Intent intent = new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png", "image/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        // Launching the Intent
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK && requestCode == GALLERY_REQUEST_CODE) {//data.getData returns the content URI for the selected Image
            selectedImage = data.getData();
            setAvatarImage(selectedImage);
        }
    }
}
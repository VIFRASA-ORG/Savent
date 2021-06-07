package com.vitandreasorino.savent;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;


public class AccountFragment extends Fragment {

    CheckBox checkBoxMaleProfile;
    CheckBox checkBoxFemaleProfile;
    CheckBox checkBoxUndefinedProfile;
    ImageView imageViewProfile;
    TextView textEditProfilePhoto;

    View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_account, container, false);
        checkBoxMaleProfile = (CheckBox) rootView.findViewById(R.id.checkBoxMaleProfile);
        checkBoxFemaleProfile = (CheckBox) rootView.findViewById(R.id.checkBoxFemaleProfile);
        checkBoxUndefinedProfile = (CheckBox) rootView.findViewById(R.id.checkBoxUndefinedProfile);
        imageViewProfile = (ImageView) rootView.findViewById(R.id.imageViewProfile);
        textEditProfilePhoto = (TextView) rootView.findViewById(R.id.textEditProfilePhoto);
        /**
         * Metodo utilizzato per gestire la selezione di una singola checkbox per volta.
         */
        checkBoxMaleProfile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    checkBoxFemaleProfile.setChecked(false);
                    checkBoxUndefinedProfile.setChecked(false);
                }
            }
        });

        checkBoxFemaleProfile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    checkBoxMaleProfile.setChecked(false);
                    checkBoxUndefinedProfile.setChecked(false);
                }
            }
        });

        checkBoxUndefinedProfile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    checkBoxMaleProfile.setChecked(false);
                    checkBoxFemaleProfile.setChecked(false);
                }
            }
        });

        textEditProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent imageProfile = new Intent();
                imageProfile.setType("image/*");
                imageProfile.setAction(Intent.ACTION_GET_CONTENT);
                // pass the constant to compare it
                // with the returned requestCode
                startActivityForResult(Intent.createChooser(imageProfile, "Select Picture"), 200);
            }
        });
        return rootView;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == -1) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == 200) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    imageViewProfile.setImageURI(selectedImageUri);
                }
            }
        }


    }

}

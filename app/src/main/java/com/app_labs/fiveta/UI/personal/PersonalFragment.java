package com.app_labs.fiveta.ui.personal;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app_labs.fiveta.R;
import com.app_labs.fiveta.model.Personal;
import com.app_labs.fiveta.model.User;
import com.app_labs.fiveta.ui.custom.CircleTransform;
import com.app_labs.fiveta.util.Constants;
import com.app_labs.fiveta.util.Utils;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PersonalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PersonalFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_LOGGED_USER = "loggedUser";
    @Bind(R.id.fab_personal_add)
    FloatingActionButton mFabPersonalAdd;
    @Bind(R.id.recyclerViewPersonalETAs)
    RecyclerView mRecyclerViewPersonalETAs;

    private User mLoggedUser;

    private DatabaseReference mRef;
    private Query mPersonalETAs;

    private FirebaseAuth mAuth;
    private LinearLayoutManager mManager;
    private FirebaseRecyclerAdapter<Personal, PersonalHolder> mRecyclerViewAdapter;


    public PersonalFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param user Parameter 1.
     * @return A new instance of fragment PersonalFragment.
     */
    public static PersonalFragment newInstance(User user) {
        PersonalFragment fragment = new PersonalFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_LOGGED_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLoggedUser = getArguments().getParcelable(ARG_LOGGED_USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_personal, container, false);
        ButterKnife.bind(this, view);

        mRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();


        populateRecyclerAdapter();

        return view;
    }

    private void populateRecyclerAdapter() {
        mManager = new LinearLayoutManager(getContext());
        mRecyclerViewPersonalETAs.setLayoutManager(mManager);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentLoggedUser = "";
        if (currentUser != null) {
            currentLoggedUser = Utils.encodeEmail(currentUser.getEmail());
        }

//        mPersonalETAs = mRef.child(Constants.PERSONAL_ETAS).orderByChild("owner").startAt(currentLoggedUser);
        mPersonalETAs = mRef.child(Constants.PERSONAL_ETAS).orderByKey().startAt("owner").orderByChild(currentLoggedUser);

        mRecyclerViewAdapter = new FirebaseRecyclerAdapter<Personal, PersonalHolder>(
                Personal.class, R.layout.item_personal, PersonalHolder.class, mPersonalETAs) {

            @Override
            public void onBindViewHolder(PersonalHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
            }


            @Override
            protected void populateViewHolder(PersonalHolder viewHolder, final Personal model, int position) {

                viewHolder.setMessage(model.getMessage());
                String key = null;
                for (Map.Entry<String, User> e : model.getSharedWith().entrySet()) {
                    key = e.getKey();
                }
                viewHolder.setImage(key);

                User sharedWith = model.getSharedWith().get(key);

                viewHolder.setName(sharedWith.getName());

                long currentETA = model.getEta() + model.getPlusEta();

                viewHolder.setEta(currentETA);

                final String personalETAKey = mRecyclerViewAdapter.getRef(position).getKey();

                viewHolder.setColor(model.getColor());

                if (model.isCompleted()) {
                    viewHolder.mFabAdd.hide();
                    viewHolder.mFabRemove.hide();
                } else {
                    viewHolder.mFabAdd.show();
                    viewHolder.mFabAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            addFiveToPlusETA(personalETAKey, model.getPlusEta());
                        }
                    });

                    viewHolder.mFabRemove.show();
                    viewHolder.mFabRemove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            removeFiveToPlusETA(personalETAKey, model.getPlusEta());
                        }
                    });
                }

                viewHolder.mImageButtonDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deletePersonalEtaNode(personalETAKey);
                    }
                });


            }
        };


        mRecyclerViewPersonalETAs.setAdapter(mRecyclerViewAdapter);
    }


    /**
     * Adds Five minutes to the plus ETA (NOT TO THE ETA, just the plus side)
     * the original ETA remains unchanged
     *
     * @param personalETAKey key from this personalETA
     * @param currentPlusETA the actual plus
     */
    private void addFiveToPlusETA(String personalETAKey, long currentPlusETA) {
        DatabaseReference databaseReference = mRef.child(Constants.PERSONAL_ETAS).child(personalETAKey);
        long addedETA = Utils.stringETAtoMilliseconds("5") + currentPlusETA;
        databaseReference.child(Constants.PLUS_ETA_FIELD_NAME).setValue(addedETA);
    }

    /**
     * Removes 5 minutes to the plus ETA
     *
     * @param personalETAKey key from this personalETA
     * @param plusEta        the actual plus
     */
    private void removeFiveToPlusETA(String personalETAKey, Long plusEta) {
        long newPlusETA = plusEta - Utils.stringETAtoMilliseconds("5"); // minus five minutes
        if (newPlusETA < 0) {
            newPlusETA = 0;
        }
        DatabaseReference databaseReference = mRef.child(Constants.PERSONAL_ETAS).child(personalETAKey);
        databaseReference.child(Constants.PLUS_ETA_FIELD_NAME).setValue(newPlusETA);
    }

    private void deletePersonalEtaNode(final String personalETAKey) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getString(R.string.dialog_delete_eta))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DatabaseReference databaseReference = mRef.child(Constants.PERSONAL_ETAS).child(personalETAKey);
                        databaseReference.setValue(null);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.fab_personal_add)
    public void onClick() {
        Intent intent = new Intent(getContext(), CreatePersonalActivity.class);
        intent.putExtra(Constants.EXTRA_USER, mLoggedUser);
        startActivity(intent);
    }

    /**
     * Holder Class
     */
    public static class PersonalHolder extends RecyclerView.ViewHolder {
        View mView;
        @Bind(R.id.linearLayoutItemPersonal)
        LinearLayout mBackgroundContainer;
        @Bind(R.id.imageViewPersonalItemContactImage)
        ImageView mImageView;
        @Bind(R.id.fab_add_personal_five)
        FloatingActionButton mFabAdd;
        @Bind(R.id.fab_remove_personal_five)
        FloatingActionButton mFabRemove;
        @Bind(R.id.textViewPersonalItemContactName)
        TextView mFriendName;
        @Bind(R.id.textViewPersonalItemETA)
        TextView mEta;
        @Bind(R.id.textViewPersonalItemMessage)
        TextView mMessage;
        @Bind(R.id.imageButtonPersonalItemDelete)
        ImageButton mImageButtonDelete;

        FirebaseStorage storage = FirebaseStorage.getInstance();


        public PersonalHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            mFabAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            mFabRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            mImageButtonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });


            mView = itemView;
        }

        public void setMessage(String message) {
            mMessage.setText(message);
        }

        public void setName(String name) {
            mFriendName.setText(name);
        }

        public void setEta(long eta) {
            if (eta > 0) {
                mEta.setText(Utils.longETAtoString(eta));
            } else {
                mEta.setText(Utils.longETAtoString(0));
            }
        }

        public void setColor(int color) {
            mBackgroundContainer.setBackgroundColor(color);
        }

        /**
         * The image name will always be the key of the shared with user such as something@gmail,com
         *
         * @param imageName
         */
        public void setImage(String imageName) {

            if (imageName != null) {

                File localFile = null;
                try {
                    localFile = new File(mImageView.getContext().getCacheDir(), imageName + ".jpg");
                    if (localFile.exists()) { // if it already exists, don't go and get it
                        Glide.with(mView.getContext())
                                .load(localFile)
                                .centerCrop()
                                .transform(new CircleTransform(mView.getContext()))
                                .into(mImageView);
                    } else {
                        assert localFile != null;
                        final File finalLocalFile = localFile;
                        // firebase references
                        StorageReference storageRef = storage.getReferenceFromUrl(Constants.FIREBASE_BUCKET);
                        StorageReference friendImageRef = storageRef.child(Constants.USER_FRIENDS_IMAGES).child(imageName + ".jpg");

                        friendImageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Glide.with(mView.getContext())
                                        .load(finalLocalFile)
                                        .centerCrop()
                                        .transform(new CircleTransform(mView.getContext()))
                                        .into(mImageView);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }
}

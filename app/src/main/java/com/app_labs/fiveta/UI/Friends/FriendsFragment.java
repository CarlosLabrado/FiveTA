package com.app_labs.fiveta.ui.Friends;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app_labs.fiveta.R;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    @BindView(R.id.friendLists)
    RecyclerView mFriendListRecyclerView;
    @BindView(R.id.fab_friend_add)
    FloatingActionButton mFabFriendAdd;

    private Unbinder unbinder;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private DatabaseReference mRef;
    private Query mUserFriends;

    private FirebaseAuth mAuth;


    private LinearLayoutManager mManager;
    private FirebaseRecyclerAdapter<User, UserHolder> mRecyclerViewAdapter;


    public FriendsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        unbinder = ButterKnife.bind(this, view);

        mRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();


        populateRecyclerAdapter();

        return view;
    }


    private void populateRecyclerAdapter() {
        mManager = new LinearLayoutManager(getContext());
        mFriendListRecyclerView.setLayoutManager(mManager);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentLoggedUser = "";
        if (currentUser != null) {
            currentLoggedUser = Utils.encodeEmail(currentUser.getEmail());
        }

        mUserFriends = mRef.child(Constants.USER_FRIENDS).child(currentLoggedUser);

        mRecyclerViewAdapter = new FirebaseRecyclerAdapter<User, UserHolder>(
                User.class, R.layout.item_friends, UserHolder.class, mUserFriends) {

            @Override
            protected void populateViewHolder(UserHolder viewHolder, final User model, int position) {
                viewHolder.setName(model.getName());
                viewHolder.setEmail(model.getEmail());
                viewHolder.setImage(Utils.encodeEmail(model.getEmail()));

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String selectedUserKey = Utils.encodeEmail(model.getEmail());
                        // TODO: maybe delete the user?
//                        createFriendRelationship(selectedUserKey);

                    }
                });

            }
        };

        mFriendListRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.fab_friend_add)
    public void onClick() {
        Intent intent = new Intent(getActivity(), com.app_labs.fiveta.ui.Friends.AddFriendActivity.class);
        startActivity(intent);
    }

    public static class UserHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageView mImageView;
        FirebaseStorage storage = FirebaseStorage.getInstance();

        public UserHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mImageView = (ImageView) itemView.findViewById(R.id.imageViewGroupFriendPicture);
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }

        public void setName(String name) {
            TextView field = (TextView) mView.findViewById(R.id.textViewFriendName);
            field.setText(name);
        }

        public void setEmail(String text) {
            TextView field = (TextView) mView.findViewById(R.id.textViewFriendEmail);
            field.setText(text);
        }

        public void setImage(String imageName) {
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

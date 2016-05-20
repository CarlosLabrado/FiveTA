package com.app_labs.fiveta.ui.personal;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app_labs.fiveta.R;
import com.app_labs.fiveta.events.SelectedFriendFromDialogEvent;
import com.app_labs.fiveta.model.User;
import com.app_labs.fiveta.util.Constants;
import com.app_labs.fiveta.util.Utils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChoseFriendDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChoseFriendDialogFragment extends DialogFragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_CURRENT_USER = "currentUser";
    @Bind(R.id.recyclerViewFriendListDialog)
    RecyclerView mRecyclerViewFriendList;

    private String mCurrentUserStringRef;

    private DatabaseReference mRef;
    private Query mUserFriends;

    private FirebaseAuth mAuth;


    private LinearLayoutManager mManager;
    private FirebaseRecyclerAdapter<User, UserHolder> mRecyclerViewAdapter;


    public ChoseFriendDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param currentUser Parameter 1.
     * @return A new instance of fragment ChoseFriendDialogFragment.
     */
    public static ChoseFriendDialogFragment newInstance(String currentUser) {
        ChoseFriendDialogFragment fragment = new ChoseFriendDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CURRENT_USER, currentUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCurrentUserStringRef = getArguments().getString(ARG_CURRENT_USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chose_friend_dialog, container, false);

        ButterKnife.bind(this, view);

        mRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();


        populateRecyclerAdapter();


        return view;

    }

    private void populateRecyclerAdapter() {
        mManager = new LinearLayoutManager(getContext());
        mRecyclerViewFriendList.setLayoutManager(mManager);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentLoggedUser = "";
        if (currentUser != null) {
            currentLoggedUser = Utils.encodeEmail(currentUser.getEmail());
        }

        mUserFriends = mRef.child(Constants.USER_FRIENDS).child(currentLoggedUser);

        mRecyclerViewAdapter = new FirebaseRecyclerAdapter<User, UserHolder>(
                User.class, R.layout.item_users, UserHolder.class, mUserFriends) {

            @Override
            protected void populateViewHolder(UserHolder viewHolder, final User model, int position) {
                viewHolder.setName(model.getName());
                viewHolder.setEmail(model.getEmail());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String selectedUserKey = Utils.encodeEmail(model.getEmail());
//                        createFriendRelationship(selectedUserKey);
                        CreatePersonalActivity.mBus.post(new SelectedFriendFromDialogEvent(model));

                    }
                });

            }
        };

        mRecyclerViewFriendList.setAdapter(mRecyclerViewAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public static class UserHolder extends RecyclerView.ViewHolder {
        View mView;

        public UserHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }

        public void setName(String name) {
            TextView field = (TextView) mView.findViewById(R.id.textViewUsersName);
            field.setText(name);
        }

        public void setEmail(String text) {
            TextView field = (TextView) mView.findViewById(R.id.textViewUsersEmail);
            field.setText(text);
        }
    }
}

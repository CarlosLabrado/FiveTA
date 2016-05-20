package com.app_labs.fiveta.ui.Friends;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app_labs.fiveta.R;
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
import butterknife.OnClick;

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
    @Bind(R.id.friendLists)
    RecyclerView mFriendListRecyclerView;
    @Bind(R.id.fab_friend_add)
    FloatingActionButton mFabFriendAdd;

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
        ButterKnife.bind(this, view);

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
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.fab_friend_add)
    public void onClick() {
        Intent intent = new Intent(getActivity(), AddFriendActivity.class);
        startActivity(intent);
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
            TextView field = (TextView) mView.findViewById(R.id.textViewFriendName);
            field.setText(name);
        }

        public void setEmail(String text) {
            TextView field = (TextView) mView.findViewById(R.id.textViewFriendEmail);
            field.setText(text);
        }
    }
}
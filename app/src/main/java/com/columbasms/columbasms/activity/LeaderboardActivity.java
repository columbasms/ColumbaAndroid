package com.columbasms.columbasms.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.columbasms.columbasms.R;
import com.columbasms.columbasms.adapter.LeaderboardAdapter;
import com.columbasms.columbasms.model.User;

import java.util.ArrayList;

/**
 * Created by Federico on 12/03/16.
 */
public class LeaderboardActivity extends AppCompatActivity {

    User user1 = new User("paolo", 300);
    User user2 = new User("ciro", 400);
    User user3 = new User("tizio", 200);
    User user4 = new User("caio", 150);
    User user5 = new User("sempronio", 30);

    ArrayList<User> users = new ArrayList<>();




    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_leaderboard);

        RecyclerView rvUsers = (RecyclerView) findViewById(R.id.rvLeaderboard);

        users.add(0, user1);
        users.add(1, user2);
        users.add(2, user3);
        users.add(3, user4);
        users.add(4, user5);

        LeaderboardAdapter adapter = new LeaderboardAdapter(users);

        rvUsers.setAdapter(adapter);

        rvUsers.setLayoutManager(new LinearLayoutManager(this));
    }



}
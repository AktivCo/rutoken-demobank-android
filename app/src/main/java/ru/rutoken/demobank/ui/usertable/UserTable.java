package ru.rutoken.demobank.ui.usertable;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.List;

import ru.rutoken.demobank.R;
import ru.rutoken.demobank.database.AppDataBase;
import ru.rutoken.demobank.database.User;
import ru.rutoken.demobank.database.UserDao;
import ru.rutoken.demobank.ui.payment.PaymentsActivity;
import ru.rutoken.demobank.ui.usertable.UserListAdapter;


public class UserTable extends AppCompatActivity {
    private UserListAdapter userListAdapter;
    private void deleteTable(){
        AppDataBase db  = AppDataBase.getdbInstance(this.getApplicationContext());
        db.userDao().deleteTable();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_table);

        Button BackToPaymentsButton = findViewById(R.id.BackToPayments);
        BackToPaymentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button DeleteTableButton = findViewById(R.id.DeleteTable);
        DeleteTableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTable();
            }
        });
        initRecyclerView();
        loadUserList();
    }



    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        userListAdapter = new UserListAdapter(this);
        recyclerView.setAdapter(userListAdapter);
    }

    private void loadUserList() {
        AppDataBase db = AppDataBase.getdbInstance(this.getApplicationContext());
        List<User> userList =db.userDao().getAllUsers();
        userListAdapter.setUserList(userList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 100) {
            loadUserList();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


}
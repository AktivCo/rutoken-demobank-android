package ru.rutoken.demobank.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM user")
    List<User> getAllUsers();


    @Query("SELECT * FROM user WHERE tokenserialnumber =:search")
    public User getNumber(String search);

    @Query("DELETE FROM user")
    void deleteTable();

    @Query("DELETE FROM user WHERE tokenserialnumber =:search")
    public void deleteUser(String search);

    @Insert
    void insertUser(User... users);

    @Delete
    void delete(User user);
}
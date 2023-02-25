package ru.rutoken.demobank.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {

    @PrimaryKey(autoGenerate = true)
    public long uid;

    @ColumnInfo(name = "TokenSerialNumber")
    public String TokenSerialNumber;

    @ColumnInfo(name = "codename")
    public byte[] codename;

    @ColumnInfo(name = "Vector")
    public String vector;

    @ColumnInfo(name = "iv")
    public byte[] iv;
}
package com.example.pawpal;

import com.example.pawpal.database.DatabaseConnection;

import java.sql.Connection;

public class DatabaseTest {

    public static void main(String[] args) {

        Connection connection =
                DatabaseConnection
                        .getInstance()
                        .getConnection();

        if (connection != null) {

            System.out.println("Connection Successful!");

        } else {

            System.out.println("Connection Failed!");

        }

    }

}
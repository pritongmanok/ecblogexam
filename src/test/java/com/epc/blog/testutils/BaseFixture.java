package com.epc.blog.testutils;

import org.junit.BeforeClass;

import java.io.IOException;

/**
 * This class is responsible for creating the basic test fixture
 * used by the different test cases.  This especially includes
 * creating the database schema.
 */
public class BaseFixture {

    private static boolean done = false;

    @BeforeClass
    public static void initialize() throws Exception {
        if(!done) {
            SQLExecutor.execute("ddl-hsqldb.sql");
            done = true;
        }

    }
}

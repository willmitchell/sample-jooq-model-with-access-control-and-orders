import com.app3.jooq.Tables;
import com.app3.jooq.tables.daos.UserDao;
import com.app3.jooq.tables.pojos.User;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.AfterClass;
import org.junit.BeforeClass
import org.testng.annotations.AfterTest
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException

import static com.app3.jooq.Tables.USER;

/**
 * Test various JOOQ facilities.
 */
public class TestJooq {

    Connection connection;
    DSLContext create;

    @BeforeTest
    void prepare() {
        try {
            def url = System.getenv("DB_URL") ?: "jdbc:postgresql:sample"
            def user = System.getenv("DB_USER") ?: "wm"
            def pass = System.getenv("DB_PASS") ?: ""
            connection = DriverManager.getConnection(url, user, pass);
            assert connection
            create = DSL.using(connection, SQLDialect.POSTGRES);
            create.settings().executeLogging=true
            assert create
        } catch (SQLException e) {
            System.out.println("e = " + e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @AfterTest
    void cleanup(){
        create.delete(USER).execute()
    }

    @Test()
    void testInsert() {
        print "In testInsert"
        assert create
        create.insertInto(USER, USER.LOGIN)
                .values("admin")
                .values("other").execute()
        print "DONE: In testInsert"
    }
}

import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.Row1;
import org.jooq.SQLDialect
import org.jooq.TransactionalRunnable;
import org.jooq.impl.DSL
import org.jooq.impl.DefaultConfiguration
import org.jooq.impl.DefaultDSLContext
import org.testng.annotations.AfterTest
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException

import static com.app3.jooq.Tables.*;
import static org.jooq.impl.DSL.*;

/**
 * Test various JOOQ facilities.
 */
public class TestJooq {

    public static final String OWNER = "owner"
    Connection connection;
    DSLContext create;
    Configuration configuration
    Random random = new Random()

    @BeforeTest
    void prepare() {
        try {
            def url = System.getenv("DB_URL") ?: "jdbc:postgresql:sample"
            def user = System.getenv("DB_USER") ?: "wm"
            def pass = System.getenv("DB_PASS") ?: ""
            connection = DriverManager.getConnection(url, user, pass);
            assert connection
            configuration = new DefaultConfiguration()
            configuration.set(connection)
            configuration.set(SQLDialect.POSTGRES)
            configuration.settings().executeLogging = true
            create = new DefaultDSLContext(configuration)
            assert create
        } catch (SQLException e) {
            System.out.println("e = " + e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        cleanup()
    }

    void cleanup() {

        [ORDER_ITEM, PRODUCT, ORDER, GROUP_ROLE, ROLE, GROUP_USER, GROUP, ASSET, ACCOUNT, USER].each {
            create.delete(it).execute()
        }
    }

    @Test()
    void testBuildModel() {
        def record = create.insertInto(USER, USER.LOGIN)
                .values(OWNER)
                .returning(USER.ID)
                .fetchOne()
        println record


        ["Mega Corp","Other Corp","Small Corp"].eachWithIndex{ it, index ->

            def aid = create.insertInto(ACCOUNT, ACCOUNT.NAME, ACCOUNT.OWNER_ID).values(it, record.getValue(USER.ID)).returning(ACCOUNT.ID).fetchOne().getValue(ACCOUNT.ID)

            def results = create.insertInto(GROUP, GROUP.ACCOUNT_ID, GROUP.NAME)
                    .values(aid, "all")
                    .values(aid, "managers")
                    .returning(GROUP.ID)
                    .fetch()
            def gids = results.collect { it.getValue(GROUP.ID) }
            def all_gid = gids[0], management_gid = gids[1]

            (1..5).each {
                def uid = create.insertInto(USER, USER.LOGIN)
                        .values("user-${index}-${it}")
                        .returning(USER.ID)
                        .fetchOne().getValue(USER.ID)

                create.insertInto(GROUP_USER, GROUP_USER.GROUP_ID, GROUP_USER.USER_ID).values(all_gid, uid).execute()
                if (it % 2) { // Half of employees are managers
                    create.insertInto(GROUP_USER, GROUP_USER.GROUP_ID, GROUP_USER.USER_ID).values(management_gid, uid).execute()
                }
            }

            def rid = create.insertInto(ROLE, ROLE.NAME).values("basic").returning(ROLE.ID).fetchOne().getValue(ROLE.ID)
            create.insertInto(GROUP_ROLE, GROUP_ROLE.GROUP_ID, GROUP_ROLE.ROLE_ID).values(all_gid, rid).execute()

            rid = create.insertInto(ROLE, ROLE.NAME).values("manage").returning(ROLE.ID).fetchOne().getValue(ROLE.ID)
            create.insertInto(GROUP_ROLE, GROUP_ROLE.GROUP_ID, GROUP_ROLE.ROLE_ID).values(management_gid, rid).execute()

            (1..10).each {
                def asid = create.insertInto(ASSET, ASSET.ACCOUNT_ID, ASSET.ADDRESS)
                        .values(aid, "address $it")
                        .returning(ASSET.ID).fetchOne().getValue(ASSET.ID)
            }

        }

        def pids = []
        (1..10).each {
            def pid = create.insertInto(PRODUCT, PRODUCT.NAME, PRODUCT.PRICE)
                    .values("product $it", 10 * it)
                    .returning(PRODUCT.ID).fetchOne().getValue(PRODUCT.ID)
            pids << pid
        }

        // Unafilliated users with 10 orders each
        (1..10).each {
            def uid = create.insertInto(USER, USER.LOGIN)
                    .values("u${it}")
                    .returning(USER.ID)
                    .fetchOne().getValue(USER.ID)

            (1..3).each {

                create.transaction(new TransactionalRunnable() {
                    @Override
                    void run(Configuration configuration) throws Exception {
                        def oid = create.insertInto(ORDER, ORDER.USER_ID, ORDER.TOTAL).values(uid, 0).returning(ORDER.ID).fetchOne().getValue(ORDER.ID)
                        (1..3).each {
                            def quantity = random.nextInt(50)
                            def pid = pids[random.nextInt(pids.size())]


                            def liid = create.insertInto(ORDER_ITEM,
                                    ORDER_ITEM.ORDER_ID,
                                    ORDER_ITEM.PRODUCT_ID,
                                    ORDER_ITEM.QUANTITY,
                                    ORDER_ITEM.AMOUNT
                            )
                                    .values(oid, pid, quantity, select(PRODUCT.PRICE).from(PRODUCT).where(PRODUCT.ID.equal(pid))).execute()

                        }
                        create.update(ORDER)
                                .set(ORDER.TOTAL, select(sum(ORDER_ITEM.AMOUNT)).from(ORDER_ITEM).where(ORDER_ITEM.ORDER_ID.equal(oid)))
                                .where(ORDER.ID.equal(oid)).execute()

                    }
                })
            }
        }

        def order_count = create.select(count()).from(ORDER).fetchOne().getValue(0)
        assert order_count

        // get assets for acc owners
        create.select(ASSET.ID).from(ASSET, ACCOUNT).where(ACCOUNT.OWNER_ID.equal(select(USER.ID).from(USER).where(USER.LOGIN.eq(OWNER)))).execute()

        // list of users that have the "manage" role for one or more companies
        def acc = ACCOUNT.as("acc")
        def ast = ASSET.as("ast")
        def g = GROUP.as("g")
        def gr = GROUP_ROLE.as("gr")
        def r = ROLE.as("r")
        def u = USER.as("u")
        def gu = GROUP_USER.as("gu")
        create.selectDistinct(u.LOGIN,r.NAME.as("role_name"),acc.NAME.as("account_name")).from(ast)
                .join(acc).on(ast.ACCOUNT_ID.eq(acc.ID))
                .join(g).on(g.ACCOUNT_ID.eq(acc.ID))
                .join(gu).on(gu.GROUP_ID.eq(g.ID))
                .join(u).on(gu.USER_ID.eq(u.ID))
                .join(gr).on(gr.GROUP_ID.eq(g.ID))
                .join(r).on(gr.GROUP_ID.eq(r.ID))
                .where(r.NAME.like("manage"))
                .orderBy(u.LOGIN)
                .execute()

        // All user role assignments and associated account name
        create.selectDistinct(u.LOGIN,r.NAME.as("role_name"),acc.NAME.as("account_name")).from(ast)
                .join(acc).on(ast.ACCOUNT_ID.eq(acc.ID))
                .join(g).on(g.ACCOUNT_ID.eq(acc.ID))
                .join(gu).on(gu.GROUP_ID.eq(g.ID))
                .join(u).on(gu.USER_ID.eq(u.ID))
                .join(gr).on(gr.GROUP_ID.eq(g.ID))
                .join(r).on(gr.GROUP_ID.eq(r.ID))
                .orderBy(u.LOGIN)
                .execute()


    }


}

package com.alibaba.druid.bvt.filter.wall.mysql;

import com.alibaba.druid.wall.WallCheckResult;
import com.alibaba.druid.wall.WallProvider;
import com.alibaba.druid.wall.spi.MySqlWallProvider;
import com.alibaba.druid.wall.spi.PGWallProvider;
import junit.framework.TestCase;
import org.joda.time.Duration;
import org.junit.Assert;

/**
 * @author Songling.Dong
 * Created on 8/8/2018.
 */
public class MySqlWallWhereNoneCheckTest  extends TestCase {
    public void test_WhereNoneCheck() {
        WallProvider provider = new MySqlWallProvider();
        provider.getConfig().setSelectWhereNoneCheck(true);

        valid("SELECT * FROM t", false);
        valid("SELECT * FROM t WHERE t.id = ?", true);
        valid("SELECT * FROM (SELECT c.* FROM c WHERE c.age > ?)t", true);
        valid("SELECT * FROM (SELECT c.* FROM ( SELECT d.* FROM d WHERE d.case > ?) c ) t", true);

        valid("SELECT * FROM (SELECT c.* FROM ( SELECT d.* FROM d) c WHERE c.time > ?) t", true);

        valid("SELECT * FROM t WHERE id = ? UNION SELECT * from t2", false);
        valid("SELECT * FROM t WHERE id = ? UNION SELECT * from t2 WHERE id = ?", true);

        valid("SELECT * FROM (SELECT * from b WHERE b.id = ? UNION SELECT cc FROM t_c )t", false);
        valid("SELECT * FROM (SELECT * from b WHERE b.id = ? UNION SELECT cc FROM t_c WHERE s = ?)t", true);

        valid("SELECT * FROM a LEFT JOIN (SELECT b.* FROM b WHERE id = ?) c ", false);
        valid("SELECT * FROM a LEFT JOIN (SELECT b.* FROM b WHERE id = ?) c WHERE c.name like '%songling'", true);
        valid("SELECT * FROM a LEFT JOIN (SELECT b.* FROM b WHERE id = ?) c WHERE c.name like '%songling'", true);
    }

    private void valid(String sql, boolean expectResult){
        WallProvider provider = new MySqlWallProvider();
        provider.getConfig().setSelectWhereNoneCheck(true);
        WallCheckResult wallCheckResult = provider.check(sql);
        Assert.assertTrue(wallCheckResult.getViolations().isEmpty() == expectResult);
    }

    public void test_2(){
        WallProvider provider = new PGWallProvider();
        provider.getConfig().setSelectWhereNoneCheck(true);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            WallCheckResult wallCheckResult = provider.check("OPEN curs1 FOR SELECT * FROM foo WHERE key = mykey;");

        }

        long x = System.currentTimeMillis() - start;

        Duration duration = new Duration(x);
        System.out.println(duration);
        System.out.println(x);
    }
}

package util;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.MyConfigure;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class MyDbUtil {
	// --数据源,整个程序中都只有这一个数据源
	private static ComboPooledDataSource dataSource;

	static {
		try {
			dataSource = new ComboPooledDataSource();
			// String driver = PropUtil.getDBdriver();
			String driver = null;
			if (MyUtils.isNull(driver)) {
				driver = "com.mysql.jdbc.Driver";
			}
			dataSource.setDriverClass(driver);
			String dbUrl = MyConfigure.getSQLurl();
			if (MyUtils.isNull(dbUrl)) {
				dbUrl = "jdbc:mysql://localhost:3308/dps1.6";
			}
			dataSource.setJdbcUrl(dbUrl);
			String user = "root", password = "root";
			dataSource.setUser(user);
			dataSource.setPassword(password);
			dataSource.setInitialPoolSize(2);
			dataSource.setMinPoolSize(1);
			dataSource.setMaxPoolSize(10);
			dataSource.setMaxStatements(50);
			dataSource.setMaxIdleTime(60);
			dataSource.setAcquireIncrement(5);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
	}

	/**
	 * javaBean的数据查询（多条数据）
	 * 
	 * @param sql
	 *            查询语句
	 * @param clazz
	 *            查询结果对象的类
	 * @param params
	 *            查询条件
	 * @return 查询结果集合
	 * @throws SQLException
	 */
	public static <T> List<T> queryBeanListData(String sql, Class<T> clazz,
			Object... params) throws SQLException {
		QueryRunner runner = new QueryRunner(dataSource);
		return runner.query(sql, new BeanListHandler<T>(clazz), params);
	}

	/**
	 * javaBean单条记录查询
	 * 
	 * @param sql
	 *            查询语句
	 * @param clazz
	 *            查询结果对象的类
	 * @param params
	 *            查询条件
	 * @return 返回查询结果
	 * @throws SQLException
	 */
	public static <T> T queryBeanData(String sql, Class<T> clazz,
			Object... params) throws SQLException {
		QueryRunner runner = new QueryRunner(dataSource);
		return runner.query(sql, new BeanHandler<T>(clazz), params);
	}

	/**
	 * 表格显示数据的查询
	 * 
	 * @param sql
	 *            查询语句
	 * @param params
	 *            查询条件
	 * @return 查询结果集合Vector<Vector<Object>>
	 * @throws SQLException
	 */
	public static List<Vector<Object>> queryTableData2(String sql,
			Object... params) throws SQLException {
		QueryRunner runner = new QueryRunner(dataSource);
		return runner.query(sql, new ResultSetHandler<List<Vector<Object>>>() {
			public List<Vector<Object>> handle(ResultSet rs)
					throws SQLException {
				List<Vector<Object>> datas = new ArrayList<Vector<Object>>();
				ResultSetMetaData rsmd = rs.getMetaData();
				while (rs.next()) {
					Vector<Object> temp = new Vector<Object>();
					for (int i = 1; i <= rsmd.getColumnCount(); i++) {
						temp.add(rs.getObject(i));
					}
					datas.add(temp);
				}
				return datas;
			}
		}, params);
	}

	/**
	 * JTable表格显示数据的查询
	 * 
	 * @param sql
	 *            查询语句
	 * @param params
	 *            查询条件
	 * @return
	 * @throws SQLException
	 */
	public static List<Vector<Object>> queryTableData(String sql,
			Object... params) throws SQLException {
		QueryRunner runner = new QueryRunner(dataSource);
		return runner.query(sql, new TableListHandler(), params);
	}

	public static List<List<String>> queryStringList(String sql,
			Object... params) throws SQLException {
		QueryRunner runner = new QueryRunner(dataSource);
		return runner.query(sql, new StringListHandler(), params);
	}

	/**
	 * 查询单列，用于查询设计编号或者仪器名称
	 * 
	 * @param sql
	 *            查询语句
	 * @param params
	 *            查询条件
	 * @return 查询设计编号集合List<Object>
	 * @throws SQLException
	 */
	public static <T> List<T> queryOneColumn(String sql, Object... params)
			throws SQLException {
		QueryRunner runner = new QueryRunner(dataSource);
		return runner.query(sql, new ColumnListHandler<T>(1), params);
	}

	/**
	 * 增删改查
	 * 
	 * @param sql
	 *            sql操作语句
	 * @param params
	 *            操作条件
	 * @return 操作成功数据条数
	 * @throws SQLException
	 */
	public static int update(String sql, Object... params) throws SQLException {
		QueryRunner runner = new QueryRunner(dataSource);
		return runner.update(sql, params);
	}

	/**
	 * 获取查询记录条数
	 * 
	 * @param sql
	 *            查询语句
	 * @param params
	 *            查询条件
	 * @return 记录条数
	 * @throws SQLException
	 */
	public static int getDataCounts(String sql, Object... params)
			throws SQLException {
		QueryRunner runner = new QueryRunner(dataSource);
		return ((Long) runner.query(sql, new ScalarHandler<Long>(), params))
				.intValue();
	}

	public static Date getTime(String sql, Object... params)
			throws SQLException {
		QueryRunner runner = new QueryRunner(dataSource);
		return ((Date) runner.query(sql, new ScalarHandler<Date>(), params));
	}

	/**
	 * 批量操作数据
	 * 
	 * @param sql
	 *            sql操作语句
	 * @param data
	 *            数据集合
	 * @throws SQLException
	 */
	public static int batchData(String sql, List<Vector<Object>> datas)
			throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		int count = datas.size();
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(sql);
			for (Vector<Object> data : datas) {
				try {
					for (int i = 0; i < data.size(); i++) {
						ps.setObject(i + 1, data.get(i));
					}
					ps.executeUpdate();
				} catch (SQLException e) {
					count--;
					// e.printStackTrace();
					continue;
				}
			}
			return count;
		} catch (SQLException e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn, ps, null);
		}
	}

	/**
	 * 导入数据（Excel）
	 * 
	 * @param sql
	 * @param datas
	 * @return
	 * @throws SQLException
	 */
	public static int importData(String sql, List<String[]> datas)
			throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		int count = datas.size();
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(sql);
			for (String[] data : datas) {
				try {
					for (int i = 0; i < data.length; i++) {
						ps.setString(i + 1, data[i]);
					}
					ps.executeUpdate();
				} catch (SQLException e) {
					count--;
				}
			}
			return count;
		} catch (SQLException e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn, ps, null);
		}
	}

	/**
	 * 批量更新时间
	 * 
	 * @param sql
	 * @param hm
	 * @throws SQLException
	 */
	public static void batchTime(String sql, HashMap<Integer, Date> hm)
			throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(sql);
			for (Entry<Integer, Date> entry : hm.entrySet()) {
				Date date = entry.getValue();
				if (date != null) {
					try {
						ps.setObject(1, date);
						ps.setObject(2, entry.getKey());
						ps.executeUpdate();
					} catch (SQLException e) {
						continue;
					}
				}
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn, ps, null);
		}
	}

}

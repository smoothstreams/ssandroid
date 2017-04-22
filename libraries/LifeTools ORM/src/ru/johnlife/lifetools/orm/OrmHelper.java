package ru.johnlife.lifetools.orm;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.johnlife.lifetools.data.AbstractData;
import ru.johnlife.lifetools.data.IdentityOrmData;
import ru.johnlife.lifetools.tools.Restart;
import ru.johnlife.lifetools.reporter.UpmobileExceptionReporter;
import ru.johnlife.lifetools.task.Task;

/**
 * Database helper class used to manage the creation and upgrading of your
 * database. This class also usually provides the DAOs used by the other
 * classes.
 */
@SuppressWarnings("unchecked")
public class OrmHelper<ID> extends OrmLiteSqliteOpenHelper {
	public static final String DB_VERSION_PREF = "version.db.orm.lifetools";
	private Map<Class<? extends AbstractData>, Dao<?, ID>> dataDaoMap = new HashMap<Class<? extends AbstractData>, Dao<?, ID>>();
	private Context context;
	/*package for visualizer*/ static List<Class<? extends AbstractData>> dataClasses;

	public OrmHelper(Context context, List<Class<? extends AbstractData>> dataClasses) {
		super(context, context.getPackageName(), null, PreferenceManager.getDefaultSharedPreferences(context).getInt(DB_VERSION_PREF, 1));
		this.context = context;
		this.dataClasses = dataClasses;
	}

	/**
	 * This is called when the database is first created. Usually you should
	 * call createTable statements here to create the tables that will store
	 * your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			for (Class<? extends AbstractData> c : dataClasses) {
				TableUtils.createTable(connectionSource, c);
			}
		} catch (SQLException e) {
			Log.e(OrmHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}

	}

	/**
	 * This is called when your application is upgraded and it has a higher
	 * version number. This allows you to adjust the various data to match the
	 * new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		Log.w("DB Upgrade", "-------------------!!!------------------\nUpgrading DB schema. All data lost\n-------------------!!!------------------\nNew schema version is "+newVersion);
		try {
			for (Class<? extends AbstractData> c : dataClasses) {
				TableUtils.dropTable(connectionSource, c, true);
			}
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(OrmHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the Dao (Database Access Object) version of a Dao
	 * for Data class. It will create it or just give the cached
	 * value. 
	 * @throws SQLException 
	 */
	public <T extends AbstractData> Dao<T, ID> getDataDao(Class<T> cls) {
		@SuppressWarnings("unchecked")
		Dao<T, ID> dao = (Dao<T, ID>) dataDaoMap.get(cls);
		if (dao == null) {
			try {
				dao = getDao(cls);
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			}
			dataDaoMap.put(cls, dao);
		}
		return dao;
	}

	protected <T extends AbstractData> Dao<T, ID> getDataDao(T what) {
		Class<T> cls = (Class<T>) what.getClass();
		return getDataDao(cls);
	}

	protected <T extends AbstractData> void deepCreateOrUpdate(T object) throws SQLException {
		Class<T> cls = (Class<T>) object.getClass();
		Dao<T, ID> dao = (Dao<T, ID>) getDataDao(cls);
		dao.createOrUpdate(object);
		Field[] fields = cls.getDeclaredFields();
		for (Field field : fields) {
			boolean acc = field.isAccessible();
			field.setAccessible(true);
			Object fieldValue;
			try {
				fieldValue = field.get(object);
			} catch (Throwable e) {
				continue;
			}
			if (fieldValue instanceof Collection) {
				for (Object o : (Collection)fieldValue) {
					if (o instanceof AbstractData) {
						deepCreateOrUpdate((AbstractData)o);
					}
				}
			}
			field.setAccessible(acc);
		}
	}

	public <T extends AbstractData> void persist(T what) {
		try {
			getDataDao(what).createOrUpdate(what);
		} catch (SQLException e) {
			handleException(e);
		}
	}

	public <T extends AbstractData> void delete(T what) {
		try {
			getDataDao(what).delete(what);
		} catch (SQLException e) {
			handleException(e);
		}
	}

	public <T extends AbstractData> List<T> getAll(Class<T> dataClass) {
		try {
			return getDataDao(dataClass).queryForAll();
		} catch (SQLException e) {
			return handleException(e);
		}
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		dataDaoMap.clear();
	}

	public <T extends AbstractData> T getOnly(Class<T> dataClass) {
		return getOnly(dataClass, null);
	}

	public <T extends AbstractData> T getOnly(Class<T> dataClass, T valueIfNull) {
		List<T> list = getAll(dataClass);
		return list.isEmpty() ? valueIfNull : list.get(0);
	}

	public <T extends AbstractData> T getById(Class<T> dataClass, ID id) {
		return getById(dataClass, id, null);
	}

	public <T extends AbstractData> T getById(Class<T> dataClass, ID id, T valueIfNull) {
		Dao<T, ID> dao = getDataDao(dataClass);
		try {
			T value = dao.queryForId(id);
			if (null == value) {
				return valueIfNull;
			} else {
				return value;
			}
		} catch (SQLException e) {
			return handleException(e);
		}
	}

	public <T extends IdentityOrmData<ID>> void addIfNotExist(T what) {
		try {
			Dao<T, ID> dao = getDataDao(what);
			if (!dao.idExists(what.getId())) {
				dao.createOrUpdate(what);
			}
		} catch (SQLException e) {
			handleException(e);
		}
	}


	private <T extends AbstractData> T handleException(SQLException e) {
		Log.e("SQL_Exception", "Error in db", e);
		UpmobileExceptionReporter.getInstance(context).logException(e);
		new Task(){
			@Override
			protected void doInBackground() {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
				int version = prefs.getInt(OrmHelper.DB_VERSION_PREF, 1);
				prefs.edit().putInt(OrmHelper.DB_VERSION_PREF, version+1).apply();
				Restart.app(context);
			}
		}.execute();
		throw new RuntimeException(e);
	}

}

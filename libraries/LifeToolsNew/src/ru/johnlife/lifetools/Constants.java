package ru.johnlife.lifetools;

import java.util.Locale;

public interface Constants {
	static final String UTF_8 = "UTF-8";
	static final Locale RUSSIAN = new Locale("ru");
	static final Locale ENGLISH_US = Locale.US;
	
	static final String PACKAGE = "ru.johnlife.lifetools.";
	static final String ACTION = PACKAGE + "action.";
	
	public static final String ACTION_REFRESH_FRAGMENT = ACTION + "refresh.fragment";
	public static final String ACTION_BACK_FRAGMENT = ACTION + "back.fragment";
	public static final String EXTRA_MASTER_NAME = "name.master.extra";
	public static final String EXTRA_MASTER_ARGS = "args.master.extra";
	public static final String EXTRA_MASTER_STATE = "state.master.extra";
	public static final String EXTRA_DETAIL_NAME = "name.detail.extra";
	public static final String EXTRA_DETAIL_ARGS = "args.detail.extra";
	public static final String EXTRA_ARGS = "args.extra";

	String EXTRA_FRAGMENT = "fragment.extra";
	String EXTRA_ARGUMENTS = "arguments.extra";

	String PREF_WELCOMED = "welcomed.pref";


	long SECOND = 1000;
	long MINUTE = 60 * SECOND;
	long HOUR = 60 * MINUTE;
	long DAY = 24 * HOUR;
	long WEEK = 7 * DAY;
	long MONTH = 30 * DAY;
	long YEAR = 365 * DAY;
}

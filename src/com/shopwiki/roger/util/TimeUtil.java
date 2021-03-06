/*
 * Copyright [2012] [ShopWiki]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shopwiki.roger.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Copied from shopwiki repo for use in {@link com.shopwiki.roger.RabbitReconnector}.
 *
 * @author rstewart
 */
public class TimeUtil {

	private static final TimeZone NY_TIMEZONE = TimeZone.getTimeZone("America/New_York");

	private static final ThreadLocal<DateFormat> _dateFormat = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			format.setTimeZone(NY_TIMEZONE);
			return format;
		}
	};

	public static String format(Date date) {
		if (date == null)
			return null;
		return _dateFormat.get().format(date);
	}

	public static String now() {
		return format(new Date());
	}
}

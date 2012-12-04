package il.yrtimid.osm.osmpoi.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class AndroidLogHandler extends Handler {

	@Override
	public void close() {
	}

	@Override
	public void flush() {
	}

	@Override
	public void publish(LogRecord record) {
		Level l = record.getLevel();
		//TODO: switch-case for different log levels
		Log.d(record.getMessage());
	}

}

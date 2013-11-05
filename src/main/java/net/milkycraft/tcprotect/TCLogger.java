package net.milkycraft.tcprotect;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TCLogger {
	Logger l;

	TCLogger(Logger l) {
		this.l = l;
	}

	public void info(String s) {
		this.l.info("tcprotect: [" + s + "]");
	}

	public void warning(String s) {
		this.l.warning("tcprotect: [" + s + "]");
	}

	public void severe(String s) {
		this.l.severe("tcprotect: [" + s + "]");
	}

	public void log(Level level, String s) {
		this.l.log(level, "tcprotect: [" + s + "]");
	}

	public void debug(String s) {
		if (TCProtect.debugMessages) {
            this.l.info("tcprotect Debug: [" + s + "]");
        }
	}
}
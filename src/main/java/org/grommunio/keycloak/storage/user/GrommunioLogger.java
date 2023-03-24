package org.grommunio.keycloak.storage.user;

//import java.util.logging.Level;

import org.jboss.logging.Logger;
//import org.jboss.logging.Logger.Level;
import org.apache.commons.lang.ArrayUtils;
import org.jboss.logmanager.ExtLogRecord;

import java.util.Arrays;
import java.util.Iterator;

public class GrommunioLogger extends Logger {
    public Level[] LEVELS;
    private org.jboss.logmanager.Logger logger;
//    public Logger logger = Logger.getLogger(GrommunioLogger.class);
    public GrommunioLogger(String name, org.jboss.logmanager.Logger logger) {
        super(name);
        this.logger = logger;
        this.initLevel();
    }

    public GrommunioLogger(String name) {
        super(name);
        this.logger = org.jboss.logmanager.Logger.getLogger(name);
        this.initLevel();
    }

    private void initLevel() {
        this.LEVELS = Level.values();
        String level = System.getProperty("logging.level", "INFO");
        if (this.contains(level)) {
            this.logger.setLevelName(level);
        }
    }

    public boolean contains(String levelName) {
        Iterator<Level> iter = Arrays.stream(this.LEVELS).iterator();
        while (iter.hasNext()) {
            Level nextLevel = iter.next();
            if (nextLevel.name().equals(levelName)) {
                return true;
            }
        }
        return false;
    }
    public static Logger getLogger(Class<?> clazz) {
//        return getLogger(clazz.getName());
        //        org.jboss.logmanager.Logger l = getLogger(clazz.getName())
        return new GrommunioLogger(clazz.getName());
    }

    public boolean isEnabled(Logger.Level level) {
        return this.logger.isLoggable(translate(level));
    }

    protected void doLog(Logger.Level level, String loggerClassName, Object message, Object[] parameters, Throwable thrown) {
        org.jboss.logmanager.Level translatedLevel = translate(level);
        if (this.logger.isLoggable(translatedLevel)) {
            if (parameters == null) {
                this.logger.log(loggerClassName, translatedLevel, String.valueOf(message), thrown);
            } else {
                this.logger.log(loggerClassName, translatedLevel, String.valueOf(message), ExtLogRecord.FormatStyle.MESSAGE_FORMAT, parameters, thrown);
            }
        }

    }

    protected void doLogf(Logger.Level level, String loggerClassName, String format, Object[] parameters, Throwable thrown) {
        if (parameters == null) {
            this.logger.log(loggerClassName, translate(level), format, thrown);
        } else {
            this.logger.log(loggerClassName, translate(level), format, ExtLogRecord.FormatStyle.PRINTF, parameters, thrown);
        }
    }

    private static org.jboss.logmanager.Level translate(Level level) {
        if (level == org.jboss.logging.Logger.Level.TRACE) {
            return org.jboss.logmanager.Level.TRACE;
        } else {
            return level == Level.DEBUG ? org.jboss.logmanager.Level.DEBUG : (org.jboss.logmanager.Level) infoOrHigher(level);
        }
    }

    private static org.jboss.logmanager.Level infoOrHigher(Level level) {
        if (level == org.jboss.logging.Logger.Level.INFO) {
            return org.jboss.logmanager.Level.INFO;
        } else if (level == org.jboss.logging.Logger.Level.WARN) {
            return org.jboss.logmanager.Level.WARN;
        } else if (level == org.jboss.logging.Logger.Level.ERROR) {
            return org.jboss.logmanager.Level.ERROR;
        } else {
            return (org.jboss.logmanager.Level)(level == org.jboss.logging.Logger.Level.FATAL ? org.jboss.logmanager.Level.FATAL : org.jboss.logmanager.Level.ALL);
        }
    }
}

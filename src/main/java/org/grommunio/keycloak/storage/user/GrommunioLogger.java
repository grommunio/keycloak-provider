// SPDX-License-Identifier: AGPL-3.0-or-later
// SPDX-FileCopyrightText: 2026 grommunio GmbH

package org.grommunio.keycloak.storage.user;

import org.jboss.logging.Logger;
import org.jboss.logmanager.ExtLogRecord;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

public class GrommunioLogger extends Logger {
    public Level[] LEVELS;
    private final org.jboss.logmanager.Logger logger;

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
        Properties conf = GrommunioConfig.getConfig();
        String level = conf.getProperty("logging.level", "INFO");
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
            return level == Level.DEBUG ? org.jboss.logmanager.Level.DEBUG : infoOrHigher(level);
        }
    }

    private static org.jboss.logmanager.Level infoOrHigher(Level level) {
        switch(level) {
            case INFO:
                return org.jboss.logmanager.Level.INFO;
            case WARN:
                return org.jboss.logmanager.Level.WARN;
            case ERROR:
                return org.jboss.logmanager.Level.ERROR;
            default:
                return (org.jboss.logmanager.Level) (level == org.jboss.logging.Logger.Level.FATAL ? org.jboss.logmanager.Level.FATAL : org.jboss.logmanager.Level.ALL);
        }
    }
}

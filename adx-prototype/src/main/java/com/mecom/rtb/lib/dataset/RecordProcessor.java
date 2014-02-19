/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.adsame.rtb.lib.dataset;

import com.adsame.rtb.lib.dataset.time.ClockTime;
import com.adsame.rtb.lib.dataset.time.DateTime;
import com.adsame.rtb.lib.dataset.time.Day;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RecordProcessor<R extends Record> {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RecordProcessor.class);

    private Class classType;
    private ArrayList<FieldMeta> fieldMetaList;
    private int numFields;
    private String fieldNames[];

    protected RecordProcessor(final Class classType) {
        this.classType = classType;

        this.fieldMetaList = new ArrayList<FieldMeta>();
        final Field fields[] = classType.getFields();
        for (Field field : fields) {
            int modifier = field.getModifiers();
            if (!Modifier.isPublic(modifier)
                    || Modifier.isStatic(modifier)
                    || Modifier.isFinal(modifier)) {
                continue;
            }

            if (!field.isAnnotationPresent(FieldAnnotation.class)) {
                continue;
            }

            FieldMeta fieldMeta = FieldMeta.createFieldMeta(field);
            fieldMetaList.add(fieldMeta);
        }

        numFields = fieldMetaList.size();

        this.fieldNames = new String[numFields];
        for (int i = 0; i < numFields; i++) {
            fieldNames[i] = fieldMetaList.get(i).getFieldName();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface FieldAnnotation {
    }

    private static abstract class FieldMeta<T, R> {

        protected static final Logger LOGGER =
                LoggerFactory.getLogger(FieldMeta.class);

        protected String className;
        protected String fieldName;

        private Field field;

        public FieldMeta(Field field) {
            this.className = getClass().getSimpleName();
            this.fieldName = field.getName();
            this.field = field;
        }

        public static <R> FieldMeta createFieldMeta(Field field) {
            FieldMeta fieldMeta = null;
            Object fieldType = field.getType();
            if (fieldType.equals(Boolean.class)) {
                fieldMeta = new BooleanFieldMeta(field);
            } else if (fieldType.equals(Integer.class)) {
                fieldMeta = new IntegerFieldMeta(field);
            } else if (fieldType.equals(Long.class)) {
                fieldMeta = new LongFieldMeta(field);
            } else if (fieldType.equals(Float.class)) {
                fieldMeta = new FloatFieldMeta(field);
            } else if (fieldType.equals(Double.class)) {
                fieldMeta = new DoubleFieldMeta(field);
            } else if (fieldType.equals(String.class)) {
                fieldMeta = new StringFieldMeta(field);
            } else if (fieldType.equals(Integer[].class)) {
                fieldMeta = new IntegerArrayFieldMeta(field);
            } else if (fieldType.equals(String[].class)) {
                fieldMeta = new StringArrayFieldMeta(field);
            } else if (fieldType.equals(BigInteger.class)) {
                fieldMeta = new BigIntegerFieldMeta(field);
            } else if (fieldType.equals(DateTime.class)) {
                fieldMeta = new DateTimeFieldMeta(field);
            } else if (fieldType.equals(Day.class)) {
                fieldMeta = new DayFieldMeta(field);
            } else if (fieldType.equals(ClockTime.class)) {
                fieldMeta = new TimeFieldMeta(field);
            } else {
                LOGGER.error("unsupported field type {}", fieldType);
            }
            return fieldMeta;
        }

        public final String getFieldName() {
            return fieldName;
        }

        public final T getValue(R record) {
            T result = null;
            try {
                result = (T) field.get(record);
            } catch (IllegalArgumentException ex) {
                logFieldAccessError(true, record, ex);
            } catch (IllegalAccessException ex) {
                logFieldAccessError(true, record, ex);
            }
            return result;
        }

        public final void setValue(R record, T value) {
            try {
                field.set(record, value);
            } catch (IllegalArgumentException ex) {
                logFieldAccessError(false, record, ex);
            } catch (IllegalAccessException ex) {
                logFieldAccessError(false, record, ex);
            }
        }

        public abstract T duplicate(R record);

        public final void parseFrom(R record, ResultSet set) {
            try {
                internalParseFrom(record, set);
            } catch (SQLException ex) {
                logResultSetGetError(set, ex);
            }
        }

        public abstract void parseFrom(R record, String text);

        public final void setStatement(R record,
                PreparedStatement statement, int insertIndex) {
            try {
                internalSetStatement(record, statement, insertIndex);
            } catch (SQLException ex) {
                logStatementSetError(record, statement, insertIndex, ex);
            }
        }

        public final void nameValueToBuilder(R record,
                StringBuilder builder) {
            builder.append(" ").append(fieldName).append(" = '").
                    append(valueToString(record)).append("',");
        }

        public String valueToString(R record) {
            T value = getValue(record);
            if (value == null) {
                return "";
            }
            return value.toString();
        }

        protected abstract void internalParseFrom(R record, ResultSet set)
                throws SQLException;

        protected abstract void internalSetStatement(R record,
                PreparedStatement statement, int insertIndex)
                throws SQLException;

        private void logFieldAccessError(boolean gets, R record, Exception ex) {
            String operation = gets ? "getting" : "setting";
            LOGGER.error("{} - {} field {} from {} failed",
                    new Object[]{className, operation,
                        fieldName, record.getClass().getName(), ex});
        }

        private void logResultSetGetError(ResultSet set, SQLException ex) {
            LOGGER.error("{} - result set {} field get failed",
                    new Object[]{className, set, ex});
        }

        private void logStatementSetError(R record, PreparedStatement statement,
                int insertIndex, SQLException ex) {
            T value = getValue(record);
            LOGGER.error("{} - statement {} set for index {} "
                    + "of value {} failed",
                    new Object[]{className, statement, insertIndex, value, ex});
        }
    }

    private static abstract class SimpleFieldMeta<T, R>
            extends FieldMeta<T, R> {

        public SimpleFieldMeta(Field field) {
            super(field);
        }

        @Override
        public final T duplicate(R record) {
            return getValue(record);
        }
    }

    private static abstract class ArrayFieldMeta<T, R>
            extends FieldMeta<T[], R> {

        private static final char COMMA = ',';
        private static final char VERTICAL = '|';

        private enum StatusSet {
            SPLIT_STATUS, ESCAPE_STATUS, NORMAL_STATUS, ERROR_STATUS
        }

        public ArrayFieldMeta(Field field) {
            super(field);
        }

        @Override
        public final T[] duplicate(R record) {
            T value[] = getValue(record);
            return Arrays.copyOf(value, value.length);
        }

        @Override
        public final void parseFrom(R record, String text) {
            parseInternal(record, text);
        }

        @Override
        public final String valueToString(R record) {
            T value[] = getValue(record);
            if (value == null || value.length == 0) {
                return "";
            }
            ArrayList<String> valueList = new ArrayList<String>();
            for(T element : value) {
                valueList.add(element.toString());
            }
            return restore(valueList);
        }

        @Override
        protected final void internalParseFrom(R record, ResultSet set)
                throws SQLException {
            String text = set.getString(fieldName);
            parseInternal(record, text);
        }

        @Override
        protected final void internalSetStatement(R record,
                PreparedStatement statement, int insertIndex)
                throws SQLException {
            statement.setString(insertIndex, valueToString(record));
        }

        protected abstract T parseSingleValue(String text);

        protected abstract void setValue(R record,
                ArrayList<T> valueList);

        private void parseInternal(R record, String text) {
            ArrayList<T> valueList;
            if (text == null || text.length() == 0) {
                valueList = new ArrayList<T>(0);
            } else {
                ArrayList<String> tokens = split(text);
                valueList = new ArrayList<T>(tokens.size());
                for (String element : tokens) {
                    valueList.add(parseSingleValue(element));
                }
            }
            setValue(record, valueList);
        }

        private ArrayList<String> split(String text) {
            ArrayList<String> tokens ;
            if (text == null || text.length() == 0) {
                tokens = new ArrayList<String>();
            } else {
                tokens = new ArrayList<String>();
                int i = 0, nextMatch = 0;
                while (i <= text.length()) {
                    switch (checkStatus(text, i)) {
                        case SPLIT_STATUS: {
                            tokens.add(escape(text.substring(nextMatch,i)));
                            nextMatch = ++i;
                            break;
                        }
                        case ESCAPE_STATUS: {
                            i = i + 2;
                            break;
                        }
                        case NORMAL_STATUS: {
                            ++i;
                            break;
                        }
                        case ERROR_STATUS: {
                            LOGGER.error("{} - split {} The format of input "
                                        + "string has a mistake",
                                        new Object[]{"ArrayFieldMeta", text});
                            return (new ArrayList<String>());
                        }
                    }
                }
            }
            return tokens;
        }

        private String restore(ArrayList<String> valueList) {
            StringBuilder builder = new StringBuilder();
            if (!valueList.isEmpty()) {
                builder.append(backEscape(valueList.get(0)));
                for (int i = 1; i < valueList.size(); i++) {
                    builder.append(COMMA).append(backEscape(valueList.get(i)));
                }
            }
            return builder.toString();
        }

        private String escape(String escapedString) {
            if(!escapedString.isEmpty()){
                escapedString = escapedString.replaceAll("\\|,", ",");
                escapedString = escapedString.replaceAll("\\|\\|", "\\|");
            }
            return escapedString;
        }

        private String backEscape(String escapedString) {
            if(!escapedString.isEmpty()){
                 escapedString = escapedString.replaceAll("\\|", "\\|\\|");
                 escapedString = escapedString.replaceAll(",", "\\|,");
             }
            return escapedString;
        }

        private StatusSet checkStatus(String text, int offset) {
            if ((offset == text.length()) || (text.charAt(offset) == COMMA)) {
                return StatusSet.SPLIT_STATUS;
            } else if (text.charAt(offset) == VERTICAL) {
                if (!checkNext(text, offset)) {
                    return StatusSet.ERROR_STATUS;
                }
                return StatusSet.ESCAPE_STATUS;
            }
            return StatusSet.NORMAL_STATUS;
         }

         private Boolean checkNext(String text, int offset) {
            return ((offset + 1) < text.length()) &&
                   ((text.charAt(offset + 1) == VERTICAL) ||
                   (text.charAt(offset + 1) == COMMA));
        }
    }

    private static class BooleanFieldMeta<R>
            extends SimpleFieldMeta<Boolean, R> {

        public BooleanFieldMeta(Field field) {
            super(field);
        }

        @Override
        public final void parseFrom(R record, String text) {
            Boolean value;
            if (text == null || text.isEmpty()) {
                value = null;
            } else {
                value = Boolean.parseBoolean(text);
            }
            setValue(record, value);
        }

        @Override
        protected final void internalParseFrom(R record, ResultSet set)
                throws SQLException {
            Object object = set.getObject(fieldName);
            Boolean value;
            if (object == null) {
                value = null;
            } else {
                value = set.getBoolean(fieldName);
            }
            setValue(record, value);
        }

        @Override
        protected final void internalSetStatement(R record,
                PreparedStatement statement, int insertIndex)
                throws SQLException {
            Boolean value = getValue(record);
            if (value == null) {
                statement.setNull(insertIndex, java.sql.Types.BOOLEAN);
            } else {
                statement.setBoolean(insertIndex, value);
            }
        }
    }

    private static class IntegerFieldMeta<R>
            extends SimpleFieldMeta<Integer, R> {

        public IntegerFieldMeta(Field field) {
            super(field);
        }

        @Override
        public final void parseFrom(R record, String text) {
            Integer value;
            if (text == null || text.isEmpty()) {
                value = null;
            } else {
                value = Integer.parseInt(text);
            }
            setValue(record, value);
        }

        @Override
        protected final void internalParseFrom(R record, ResultSet set)
                throws SQLException {
            Object object = set.getObject(fieldName);
            Integer value;
            if (object == null) {
                value = null;
            } else {
                value = set.getInt(fieldName);
            }
            setValue(record, value);
        }

        @Override
        protected final void internalSetStatement(R record,
                PreparedStatement statement, int insertIndex)
                throws SQLException {
            Integer value = getValue(record);
            if (value == null) {
                statement.setNull(insertIndex, java.sql.Types.INTEGER);
            } else {
                statement.setInt(insertIndex, value);
            }
        }
    }

    private static class LongFieldMeta<R>
            extends SimpleFieldMeta<Long, R> {

        public LongFieldMeta(Field field) {
            super(field);
        }

        @Override
        public final void parseFrom(R record, String text) {
            Long value;
            if (text == null || text.isEmpty()) {
                value = null;
            } else {
                value = Long.parseLong(text);
            }
            setValue(record, value);
        }

        @Override
        protected final void internalParseFrom(R record, ResultSet set)
                throws SQLException {
            Object object = set.getObject(fieldName);
            Long value;
            if (object == null) {
                value = null;
            } else {
                value = set.getLong(fieldName);
            }
            setValue(record, value);
        }

        @Override
        protected final void internalSetStatement(R record,
                PreparedStatement statement, int insertIndex)
                throws SQLException {
            Long value = getValue(record);
            if (value == null) {
                statement.setNull(insertIndex, java.sql.Types.BIGINT);
            } else {
                statement.setLong(insertIndex, value);
            }
        }
    }

    private static class FloatFieldMeta<R>
            extends SimpleFieldMeta<Float, R> {

        public FloatFieldMeta(Field field) {
            super(field);
        }

        @Override
        public final void parseFrom(R record, String text) {
            Float value;
            if (text == null || text.isEmpty()) {
                value = null;
            } else {
                value = Float.parseFloat(text);
            }
            setValue(record, value);
        }

        @Override
        protected final void internalParseFrom(R record, ResultSet set)
                throws SQLException {
            Object object = set.getObject(fieldName);
            Float value;
            if (object == null) {
                value = null;
            } else {
                value = set.getFloat(fieldName);
            }
            setValue(record, value);
        }

        @Override
        protected final void internalSetStatement(R record,
                PreparedStatement statement, int insertIndex)
                throws SQLException {
            Float value = getValue(record);
            if (value == null) {
                statement.setNull(insertIndex, java.sql.Types.FLOAT);
            } else {
                statement.setFloat(insertIndex, value);
            }
        }
    }

    private static class DoubleFieldMeta<R>
            extends SimpleFieldMeta<Double, R> {

        public DoubleFieldMeta(Field field) {
            super(field);
        }

        @Override
        public final void parseFrom(R record, String text) {
            Double value;
            if (text == null || text.isEmpty()) {
                value = null;
            } else {
                value = Double.parseDouble(text);
            }
            setValue(record, value);
        }

        @Override
        protected final void internalParseFrom(R record, ResultSet set)
                throws SQLException {
            Object object = set.getObject(fieldName);
            Double value;
            if (object == null) {
                value = null;
            } else {
                value = set.getDouble(fieldName);
            }
            setValue(record, value);
        }

        @Override
        protected final void internalSetStatement(R record,
                PreparedStatement statement, int insertIndex)
                throws SQLException {
            Double value = getValue(record);
            if (value == null) {
                statement.setNull(insertIndex, java.sql.Types.DOUBLE);
            } else {
                statement.setDouble(insertIndex, value);
            }
        }
    }

    private static class StringFieldMeta<R>
            extends SimpleFieldMeta<String, R> {

        public StringFieldMeta(Field field) {
            super(field);
        }

        @Override
        public final void parseFrom(R record, String text) {
            String value = text;
            setValue(record, value);
        }

        @Override
        protected final void internalParseFrom(R record, ResultSet set)
                throws SQLException {
            String value = set.getString(fieldName);
            setValue(record, value);
        }

        @Override
        protected final void internalSetStatement(R record,
                PreparedStatement statement, int insertIndex)
                throws SQLException {
            statement.setString(insertIndex, getValue(record));
        }
    }

    private static class BigIntegerFieldMeta<R>
            extends FieldMeta<BigInteger, R> {

        public BigIntegerFieldMeta(Field field) {
            super(field);
        }

        @Override
        public final BigInteger duplicate(R record) {
            BigInteger value = getValue(record);
            if (value == null) {
                return null;
            }
            return new BigInteger(value.toByteArray());
        }

        @Override
        public final void parseFrom(R record, String text) {
            BigInteger value;
            if (text == null || text.isEmpty()) {
                value = null;
            } else {
                value = new BigInteger(text);
            }
            setValue(record, value);
        }

        @Override
        protected final void internalParseFrom(R record, ResultSet set)
                throws SQLException {
            BigDecimal columnValue = set.getBigDecimal(fieldName);
            BigInteger value;
            if (columnValue == null) {
                value = null;
            } else {
                value = columnValue.toBigInteger();
            }
            setValue(record, value);
        }

        @Override
        protected final void internalSetStatement(R record,
                PreparedStatement statement, int insertIndex)
                throws SQLException {
            BigInteger value = getValue(record);
            if (value == null) {
                statement.setBigDecimal(insertIndex, null);
            } else {
                statement.setBigDecimal(insertIndex, new BigDecimal(value));
            }
        }
    }

    private static class StringArrayFieldMeta<R>
            extends ArrayFieldMeta<String, R> {

        public StringArrayFieldMeta(Field field) {
            super(field);
        }

        @Override
        protected final String parseSingleValue(String text) {
            return text;
        }

        @Override
        protected final void setValue(R record, ArrayList<String> valueList) {
            String value[] = new String[valueList.size()];
            valueList.toArray(value);
            setValue(record, value);
        }
    }

    private static class IntegerArrayFieldMeta<R>
            extends ArrayFieldMeta<Integer, R> {

        public IntegerArrayFieldMeta(Field field) {
            super(field);
        }

        @Override
        protected final Integer parseSingleValue(String text) {
            return Integer.parseInt(text.trim());
        }

        @Override
        protected final void setValue(R record, ArrayList<Integer> valueList) {
            Integer value[] = new Integer[valueList.size()];
            valueList.toArray(value);
            setValue(record, value);
        }
    }

    private static class DateTimeFieldMeta<R>
            extends FieldMeta<DateTime, R> {

        public DateTimeFieldMeta(Field field) {
            super(field);
        }

        @Override
        public final DateTime duplicate(R record) {
            DateTime value = getValue(record);
            if (value == null) {
                return null;
            }
            return (DateTime) value.clone();
        }

        @Override
        public final void parseFrom(R record, String text) {
            DateTime value;
            if (text == null || text.isEmpty()) {
                value = null;
            } else {
                value = DateTime.valueOf(text);
            }
            setValue(record, value);
        }

        @Override
        protected final void internalParseFrom(R record, ResultSet set)
                throws SQLException {
            DateTime value = null;
            Timestamp dbValue = set.getTimestamp(fieldName);
            if (dbValue != null) {
                value = new DateTime(dbValue.getTime());
            }
            setValue(record, value);
        }

        @Override
        protected final void internalSetStatement(R record,
                PreparedStatement statement, int insertIndex)
                throws SQLException {
            Timestamp timestamp = null;
            DateTime value = getValue(record);
            if (value != null) {
                timestamp = new Timestamp(value.getTime());
            }
            statement.setTimestamp(insertIndex, timestamp);
        }
    }

    private static class DayFieldMeta<R>
            extends FieldMeta<Day, R> {

        public DayFieldMeta(Field field) {
            super(field);
        }

        @Override
        public final Day duplicate(R record) {
            Day value = getValue(record);
            if (value == null) {
                return null;
            }
            return (Day) value.clone();
        }

        @Override
        public final void parseFrom(R record, String text) {
            Day value;
            if (text == null || text.isEmpty()) {
                value = null;
            } else {
                value = Day.valueOf(text);
            }
            setValue(record, value);
        }

        @Override
        protected final void internalParseFrom(R record, ResultSet set)
                throws SQLException {
            Day value = null;
            Date dbValue = set.getDate(fieldName);
            if (dbValue != null) {
                value = new Day(dbValue.getTime());
            }
            setValue(record, value);
        }

        @Override
        protected final void internalSetStatement(R record,
                PreparedStatement statement, int insertIndex)
                throws SQLException {
            Date date = null;
            Day value = getValue(record);
            if (value != null) {
                date = new Date(value.getTime());
            }
            statement.setDate(insertIndex, date);
        }
    }

    private static class TimeFieldMeta<R>
            extends FieldMeta<ClockTime, R> {

        public TimeFieldMeta(Field field) {
            super(field);
        }

        @Override
        public final ClockTime duplicate(R record) {
            ClockTime value = getValue(record);
            if (value == null) {
                return null;
            }
            return (ClockTime) value.clone();
        }

        @Override
        public final void parseFrom(R record, String text) {
            ClockTime value;
            if (text == null || text.isEmpty()) {
                value = null;
            } else {
                value = new ClockTime(text);
            }
            setValue(record, value);
        }

        @Override
        protected final void internalParseFrom(R record, ResultSet set)
                throws SQLException {
            ClockTime value = null;
            java.sql.Time dbValue = set.getTime(fieldName);
            if (dbValue != null) {
                value = new ClockTime(dbValue.getTime());
            }
            setValue(record, value);
        }

        @Override
        protected final void internalSetStatement(R record,
                PreparedStatement statement, int insertIndex)
                throws SQLException {
            String timeString = null;
            ClockTime time = getValue(record);
            if (time != null) {
                timeString = time.toString();
            }
            statement.setString(insertIndex, timeString);
        }
    }

    public int getNumFields() {
        return numFields;
    }

    public String[] getFieldNames() {
        return fieldNames;
    }

    public R duplicate(final R record) {
        R newRecord = createRecord(classType);
        if (record == null) {
            return null;
        }
        for (FieldMeta fieldMeta : fieldMetaList) {
            fieldMeta.setValue(newRecord, fieldMeta.getValue(record));
        }
        return newRecord;
    }

    public R parseFrom(final ResultSet set) {
        R record = createRecord(classType);
        if (record == null) {
            return null;
        }
        for (FieldMeta fieldMeta : fieldMetaList) {
            fieldMeta.parseFrom(record, set);
        }
        return record;
    }

    public R parseFrom(final String values[],
            HashMap<String, Integer> indexMap) {
        if (values.length != fieldMetaList.size()) {
            LOGGER.error("{} - csv parse failed - column number invalid",
                    classType.getName());
            return null;
        }
        R record = createRecord(classType);
        if (record == null) {
            return null;
        }
        for (FieldMeta fieldMeta : fieldMetaList) {
            String fieldName = fieldMeta.getFieldName();
            if (indexMap.containsKey(fieldName)) {
                int valueIndex = indexMap.get(fieldName);
                fieldMeta.parseFrom(record, values[valueIndex]);
            } else {
                LOGGER.error("{} - csv parse failed - column {} not exist",
                        classType.getName(), fieldName);
                return null;
            }
        }
        return record;
    }

    public void setStatement(final R record,
            final PreparedStatement statement) {
        // parameterIndex - the first parameter is 1, the second is 2, ...
        int insertIndex = 1;
        for (FieldMeta fieldMeta : fieldMetaList) {
            fieldMeta.setStatement(record, statement, insertIndex);
            insertIndex++;
        }
    }

    public String[] valuesToStringArray(final R record) {
        String values[] = new String[fieldMetaList.size()];
        int index = 0;
        for (FieldMeta fieldMeta : fieldMetaList) {
            values[index++] = fieldMeta.valueToString(record);
        }
        return values;
    }

    public String toString(final R record) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (FieldMeta fieldMeta : fieldMetaList) {
            fieldMeta.nameValueToBuilder(record, builder);
        }
        builder.append(" }");
        return builder.toString();
    }

    private R createRecord(final Class classType) {
        R record = null;
        try {
            record = (R) classType.newInstance();
        } catch (InstantiationException ex) {
            logRecordCreationError(classType, ex);
        } catch (IllegalAccessException ex) {
            logRecordCreationError(classType, ex);
        }
        return record;
    }

    private static void logRecordCreationError(Class classType, Exception ex) {
        LOGGER.error("instance of class {} create failed",
                classType.getName(), ex);
    }
}

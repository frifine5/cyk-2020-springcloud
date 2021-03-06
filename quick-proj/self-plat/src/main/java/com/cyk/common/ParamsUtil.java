package com.cyk.common;

import javax.crypto.Cipher;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 参数检查辅助工具类
 *
 * @author WangChengyu
 * 2018/5/24 15:05
 */
public class ParamsUtil {

    private static final SimpleDateFormat dateFormat10 = null;
    private static final SimpleDateFormat dateFormat19 = null;
    private static final SimpleDateFormat dfymshms = null;


    /**
     * 校验地区码，只有都符合时返回true
     */
    public static boolean isDistrctCode(String ... code ){
        for (int i = 0; i < code.length; i++) {
            if(!code[i].matches("\\d{6}")){
                return false;
            }
        }
        return true;
    }

    /**
     * 判断传入的字符src中是否含有lim字符规则，包含返回true，不包含返回false
     */
    public static boolean checkHasSpecCtx(String src, String lim) {
        Pattern p = Pattern.compile(lim);
        Matcher m = p.matcher(src);
        return m.find();
    }

    /**
     * 检查日期格式 yyyy-MM-dd
     */
    public static final String el_yyyyMMdd = "^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))";
    public static boolean checkDataStrFormat(String dataStr){
        return dataStr.matches("\\d{4}-\\d{2}-\\d{2}") && dataStr.matches(el_yyyyMMdd);
    }


    /**
     * 有任意入参为NULL或者为空字符串，即返回true
     */
    public static boolean checkNull(String... args) {
        for (String str : args) {
            if (null == str || "".equals(str))
                return true;
        }
        return false;
    }


    public static boolean checkNullSymbal(String... args) {
        for (String str : args) {
            if (null == str || "".equals(str) || "null".equalsIgnoreCase(str) || "undefined".equalsIgnoreCase(str))
                return true;
        }
        return false;
    }

    /**
     * 列表为空则返回true，不为空，有子元素则返回false
     */
    public static boolean checkListNull(List list){
        return (null == list || list.size()<=0);
    }

    /**
     * 所有入参为NULL或者为空字符串，返回true；任一有非空字符即为false
     */
    public static boolean allNull(String... args) {
        boolean rtn = true;
        for (String str : args) {
            if (null != str && !"".equals(str)) {
                rtn = false;
                break;
            }
        }
        return rtn;
    }

    /**
     * 格式化时间为10位日期("yyyy-MM-dd")
     */
    public static String formatData10(Date date) {
        return getDateFormat10Instance().format(date);
    }

    /**
     * 格式化时间为19位日期时间("yyyy-MM-dd HH:mm:ss")
     *
     * @param date
     * @return
     */
    public static String formatTime19(Date date) {
        return getDateFormat19Instance().format(date);
    }

    /**
     * 格式化时间为14位日期时间("yyyyMMddHHmmss")
     *
     * @param date
     * @return
     */
    public static String formatTime14(Date date) {
        return getDateFormat14Instance().format(date);
    }



    /**
     * 返回list的指定连续子集；用于分页请求(不依赖数据库|对数据结果进行cut)
     */
    public static List rtnList(int stIdx, int len, List list) {
        stIdx = stIdx <0 ? 0: stIdx;
        int endIdx;
        len = len<=0 ? 10:len;
        if(stIdx>= list.size()){
            stIdx = list.size()/len * len;
            endIdx = list.size();
        }else{
            endIdx = stIdx + len;
        }
        endIdx = endIdx<list.size()?endIdx:list.size();
        return list.subList(stIdx, endIdx);
    }



    private static SimpleDateFormat getDateFormat10Instance() {
        if (null == dateFormat10) {
            return new SimpleDateFormat("yyyy-MM-dd");
        } else {
            return dateFormat10;
        }
    }

    private static SimpleDateFormat getDateFormat19Instance() {
        if (null == dateFormat19) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        } else {
            return dateFormat19;
        }
    }

    private static SimpleDateFormat getDateFormat14Instance() {
        if (null == dfymshms) {
            return new SimpleDateFormat("yyyyMMddHHmmss");
        } else {
            return dfymshms;
        }
    }

    /**
     * 返回去除-的uuid字符
     */
    public static String getUUIDStr() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }


    /**
     * 十六进制字符检查
     */
    public static boolean isHexString(String in){
        String reg = "^[0-9abcdefABCDEF]+";
        if(in.matches(reg)){
            return true;
        }else{
            return false;
        }
    }


    /**
     * Base64字符检查
     */
    public static boolean isBase64Str(String in){
        String base64Pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
        return Pattern.matches(base64Pattern, in);
    }

    public static byte[] padding(byte[] src, int eod){
        int len = src.length;
        if(eod == Cipher.ENCRYPT_MODE){// 加密前补码
            int p = 16 - len % 16;
            byte[] out = new byte[len + p];
            System.arraycopy(src, 0, out, 0, len);
            for(int i= 0; i < p; i++){
                out[len + i]= (byte)p;
            }
            return out;
        }else{
            // 检查是否满足已补码的规则
            int p = src[len-1];
            if(p>16 || p<1){
                throw new RuntimeException("数据不符合补位规则，无法去除补位");
            }else{
                for(int i = len-1; i >= len - p; i--){
                    if(p != src[i]){
                        throw new RuntimeException("数据不符合补位规则，无法去除补位");
                    }
                }
                // 截取有效部分
                return Arrays.copyOfRange(src, 0, len-p);
            }
        }
    }


    private final static String[] sqlKeys = {
            "action", "add", "aggregate", "all", "alter", "after", "and", "as", "asc", "avg",
            "avg_row_length", "auto_increment", "between", "bigint", "bit", "binary", "blob", "bool", "both", "by",
            "cascade", "case", "char", "character", "change", "check", "checksum", "column", "columns", "comment",
            "constraint", "create", "cross", "current_date", "current_time", "current_timestamp", "data", "database", "databases", "date",
            "datetime", "day", "day_hour", "day_minute", "day_second", "dayofmonth", "dayofweek", "dayofyear", "dec", "decimal",
            "default", "delayed", "delay_key_write", "delete", "desc", "describe", "distinct", "distinctrow", "double", "drop",
            "end", "else", "escape", "escaped", "enclosed", "enum", "explain", "exists", "fields", "file",
            "first", "float", "float4", "float8", "flush", "foreign", "from", "for", "full", "function",
            "global", "grant", "grants", "group", "having", "heap", "high_priority", "hour", "hour_minute", "hour_second",
            "hosts", "identified", "ignore", "in", "index", "infile", "inner", "insert", "insert_id", "int",
            "integer", "interval", "int1", "int2", "int3", "int4", "int8", "into", "if", "is",
            "isam", "join", "key", "keys", "kill", "last_insert_id", "leading", "left", "length", "like",
            "lines", "limit", "load", "local", "lock", "logs", "long", "longblob", "longtext", "low_priority",
            "max", "max_rows", "match", "mediumblob", "mediumtext", "mediumint", "middleint", "min_rows", "minute", "minute_second",
            "modify", "month", "monthname", "myisam", "natural", "numeric", "no", "not", "null", "on",
            "optimize", "option", "optionally", "or", "order", "outer", "outfile", "pack_keys", "partial", "password",
            "precision", "primary", "procedure", "process", "processlist", "privileges", "read", "real", "references", "reload",
            "regexp", "rename", "replace", "restrict", "returns", "revoke", "rlike", "row", "rows", "second",
            "select", "set", "show", "shutdown", "smallint", "soname", "sql_big_tables", "sql_big_selects", "sql_low_priority_updates", "sql_log_off",
            "sql_log_update", "sql_select_limit", "sql_small_result", "sql_big_result", "sql_warnings", "straight_join", "starting", "status", "string", "table",
            "tables", "temporary", "terminated", "text", "then", "time", "timestamp", "tinyblob", "tinytext", "tinyint",
            "trailing", "to", "type", "use", "using", "unique", "unlock", "unsigned", "update", "usage",
            "values", "varchar", "variables", "varying", "varbinary", "with", "write", "when", "where", "year",
            "year_month", "zerofill"
    };

    /**
     * sql关键字检查: false-param不含关键字； true-含
     * @param param 待检查参数
     * @return boolean
     */
    public static boolean chkSqlKeys(String param) {
        if(checkNull(param)) return false;
        if(param.contains(",")) return true;
        if(param.contains("\'")) return true;
        if(param.contains("\"")) return true;
        if(param.contains(";")) return true;
        String cp = param.toLowerCase();
        for(String key: sqlKeys){
            if(cp.contains(key)){
                return true;
            }
        }
        return false;
    }


}

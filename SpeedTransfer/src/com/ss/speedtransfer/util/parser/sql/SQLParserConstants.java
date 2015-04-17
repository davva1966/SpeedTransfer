/* Generated By:JavaCC: Do not edit this line. SQLParserConstants.java */
// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

public interface SQLParserConstants {

	int EOF = 0;
	int STAR = 7;
	int ASSIGNMENT = 8;
	int EQUALS = 9;
	int GR = 10;
	int LE = 11;
	int GREQ = 12;
	int LEEQ = 13;
	int NOTEQ = 14;
	int DIVIDE = 15;
	int ADD = 16;
	int SUBTRACT = 17;
	int CONCAT = 18;
	int BOOLEAN_LITERAL = 19;
	int NULL_LITERAL = 20;
	int REGEX_LITERAL = 21;
	int DROP = 22;
	int SHOW = 23;
	int ALTER = 24;
	int SELECT = 25;
	int UPDATE = 26;
	int CREATE = 27;
	int DELETE = 28;
	int INSERT = 29;
	int COMMIT = 30;
	int COMPACT = 31;
	int EXPLAIN = 32;
	int ROLLBACK = 33;
	int OPTIMIZE = 34;
	int DESCRIBE = 35;
	int SHUTDOWN = 36;
	int PREPARE = 37;
	int DECLARE = 38;
	int SCROLL = 39;
	int CURSOR = 40;
	int OPEN = 41;
	int FETCH = 42;
	int FIRST = 43;
	int NEXT = 44;
	int PRIOR = 45;
	int LAST = 46;
	int WHENEVER = 47;
	int SQLERROR = 48;
	int FOUND = 49;
	int GOTO = 50;
	int CLOSE = 51;
	int IS = 52;
	int AS = 53;
	int ON = 54;
	int IF = 55;
	int TO = 56;
	int NO = 57;
	int ALL = 58;
	int ANY = 59;
	int SET = 60;
	int USE = 61;
	int ASC = 62;
	int OLD = 63;
	int NEW = 64;
	int SQLADD = 65;
	int FOR = 66;
	int ROW = 67;
	int EACH = 68;
	int CALL = 69;
	int BOTH = 70;
	int SOME = 71;
	int FROM = 72;
	int LEFT = 73;
	int DESC = 74;
	int INTO = 75;
	int JOIN = 76;
	int TRIM = 77;
	int VIEW = 78;
	int LOCK = 79;
	int WITH = 80;
	int USER = 81;
	int CAST = 82;
	int LONG = 83;
	int NAME = 84;
	int JAVA = 85;
	int AFTER = 86;
	int START = 87;
	int COUNT = 88;
	int AVG = 89;
	int MAX = 90;
	int MIN = 91;
	int SUM = 92;
	int WHERE = 93;
	int CYCLE = 94;
	int CACHE = 95;
	int RIGHT = 96;
	int TABLE = 97;
	int LIMIT = 98;
	int INNER = 99;
	int INDEX = 100;
	int CROSS = 101;
	int OUTER = 102;
	int CHECK = 103;
	int USING = 104;
	int UNION = 105;
	int GRANT = 106;
	int USAGE = 107;
	int SQLRETURN = 108;
	int BEFORE = 109;
	int UNLOCK = 110;
	int ACTION = 111;
	int GROUPS = 112;
	int REVOKE = 113;
	int OPTION = 114;
	int PUBLIC = 115;
	int EXCEPT = 116;
	int IGNORE = 117;
	int SCHEMA = 118;
	int EXISTS = 119;
	int NOTEXISTS = 120;
	int VALUES = 121;
	int HAVING = 122;
	int UNIQUE = 123;
	int SQLCOLUMN = 124;
	int RETURNS = 125;
	int ACCOUNT = 126;
	int LEADING = 127;
	int NATURAL = 128;
	int BETWEEN = 129;
	int TRIGGER = 130;
	int SQLDEFAULT = 131;
	int VARYING = 132;
	int EXECUTE = 133;
	int CALLBACK = 134;
	int MINVALUE = 135;
	int MAXVALUE = 136;
	int FUNCTION = 137;
	int SEQUENCE = 138;
	int RESTRICT = 139;
	int PASSWORD = 140;
	int TRAILING = 141;
	int GROUPBY = 142;
	int ORDERBY = 143;
	int DEFERRED = 144;
	int DISTINCT = 145;
	int TOP = 146;
	int LANGUAGE = 147;
	int INCREMENT = 148;
	int PROCEDURE = 149;
	int CHARACTER = 150;
	int IMMEDIATE = 151;
	int INITIALLY = 152;
	int TEMPORARY = 153;
	int INTERSECT = 154;
	int PRIVILEGES = 155;
	int CONSTRAINT = 156;
	int DEFERRABLE = 157;
	int REFERENCES = 158;
	int PRIMARY = 159;
	int FOREIGN = 160;
	int KEY = 161;
	int INDEX_NONE = 162;
	int INDEX_BLIST = 163;
	int GROUPMAX = 164;
	int COLLATE = 165;
	int PRIMARY_STRENGTH = 166;
	int SECONDARY_STRENGTH = 167;
	int TERTIARY_STRENGTH = 168;
	int IDENTICAL_STRENGTH = 169;
	int NO_DECOMPOSITION = 170;
	int CANONICAL_DECOMPOSITION = 171;
	int FULL_DECOMPOSITION = 172;
	int BIT = 173;
	int INT = 174;
	int REAL = 175;
	int CLOB = 176;
	int BLOB = 177;
	int CHAR = 178;
	int TEXT = 179;
	int DATE = 180;
	int TIME = 181;
	int FLOAT = 182;
	int BIGINT = 183;
	int DOUBLE = 184;
	int STRING = 185;
	int BINARY = 186;
	int NUMERIC = 187;
	int DECIMAL = 188;
	int BOOLEAN = 189;
	int TINYINT = 190;
	int INTEGER = 191;
	int VARCHAR = 192;
	int SMALLINT = 193;
	int VARBINARY = 194;
	int TIMESTAMP = 195;
	int JAVA_OBJECT = 196;
	int LONGVARCHAR = 197;
	int LONGVARBINARY = 198;
	int TRANSACTIONISOLATIONLEVEL = 199;
	int AUTOCOMMIT = 200;
	int READCOMMITTED = 201;
	int READUNCOMMITTED = 202;
	int REPEATABLEREAD = 203;
	int SERIALIZABLE = 204;
	int CASCADE = 205;
	int CURRENT_TIME = 206;
	int CURRENT_DATE = 207;
	int CURRENT_TIMESTAMP = 208;
	int LIKE = 209;
	int REGEX = 210;
	int AND = 211;
	int OR = 212;
	int IN = 213;
	int NOT = 214;
	int WITH_NC = 215;
	int FOR_FETCH_ONLY = 216;
	int NUMBER_LITERAL = 217;
	int STRING_LITERAL = 218;
	int QUOTED_VARIABLE = 219;
	int IDENTIFIER = 220;
	int VARIABLE = 221;
	int DOT_DELIMINATED_REF = 222;
	int SLASH_DELIMINATED_REF = 223;
	int QUOTED_DELIMINATED_REF = 224;
	int QUOTED_SLASH_DELIMINATED_REF = 225;
	int JAVA_OBJECT_ARRAY_REF = 226;
	int CTALIAS = 227;
	int GLOBVARIABLE = 228;
	int QUOTEDGLOBVARIABLE = 229;
	int PARAMETER_REF = 230;
	int LETTER = 231;
	int DIGIT = 232;

	int DEFAULT = 0;

	String[] tokenImage = { "<EOF>", "\" \"", "\"\\t\"", "\"\\n\"", "\"\\r\"", "<token of kind 5>", "<token of kind 6>", "\"*\"", "\"=\"", "\"==\"", "\">\"", "\"<\"", "\">=\"", "\"<=\"", "<NOTEQ>", "\"/\"", "\"+\"", "\"-\"", "\"||\"", "<BOOLEAN_LITERAL>", "\"null\"", "<REGEX_LITERAL>", "\"drop\"",
			"\"show\"", "\"alter\"", "\"select\"", "\"update\"", "\"create\"", "\"delete\"", "\"insert\"", "\"commit\"", "\"compact\"", "\"explain\"", "\"rollback\"", "\"optimize\"", "\"describe\"", "\"shutdown\"", "\"prepare\"", "\"declare\"", "\"scroll\"", "\"cursor\"", "\"open\"", "\"fetch\"",
			"\"first\"", "\"next\"", "\"prior\"", "\"last\"", "\"whenever\"", "\"sqlerror\"", "\"found\"", "\"goto\"", "\"close\"", "\"is\"", "\"as\"", "\"on\"", "\"if\"", "\"to\"", "\"no\"", "\"all\"", "\"any\"", "\"set\"", "\"use\"", "\"asc\"", "\"old\"", "\"new\"", "\"add\"", "\"for\"",
			"\"row\"", "\"each\"", "\"call\"", "\"both\"", "\"some\"", "\"from\"", "\"left\"", "\"desc\"", "\"into\"", "\"join\"", "\"trim\"", "\"view\"", "\"lock\"", "\"with\"", "\"user\"", "\"cast\"", "\"long\"", "\"name\"", "\"java\"", "\"after\"", "\"start\"", "\"count\"", "\"avg\"", "\"max\"",
			"\"min\"", "\"sum\"", "\"where\"", "\"cycle\"", "\"cache\"", "\"right\"", "\"table\"", "\"limit\"", "\"inner\"", "\"index\"", "\"cross\"", "\"outer\"", "\"check\"", "\"using\"", "\"union\"", "\"grant\"", "\"usage\"", "\"return\"", "\"before\"", "\"unlock\"", "\"action\"", "\"groups\"",
			"\"revoke\"", "\"option\"", "\"public\"", "\"except\"", "\"ignore\"", "\"schema\"", "\"exists\"", "<NOTEXISTS>", "\"values\"", "\"having\"", "\"unique\"", "\"column\"", "\"returns\"", "\"account\"", "\"leading\"", "\"natural\"", "\"between\"", "\"trigger\"", "\"default\"",
			"\"varying\"", "\"execute\"", "\"callback\"", "\"minvalue\"", "\"maxvalue\"", "\"function\"", "\"sequence\"", "\"restrict\"", "\"password\"", "\"trailing\"", "\"group by\"", "\"order by\"", "\"deferred\"", "\"distinct\"", "\"top\"", "\"language\"", "\"increment\"", "\"procedure\"",
			"\"character\"", "\"immediate\"", "\"initially\"", "\"temporary\"", "\"intersect\"", "\"privileges\"", "\"constraint\"", "\"deferrable\"", "\"references\"", "\"primary\"", "\"foreign\"", "\"key\"", "\"index_none\"", "\"index_blist\"", "\"group max\"", "\"collate\"",
			"\"primary_strength\"", "\"secondary_strength\"", "\"tertiary_strength\"", "\"identical_strength\"", "\"no_decomposition\"", "\"canonical_decomposition\"", "\"full_decomposition\"", "\"bit\"", "\"int\"", "\"real\"", "\"clob\"", "\"blob\"", "\"char\"", "\"text\"", "\"date\"", "\"time\"",
			"\"float\"", "\"bigint\"", "\"double\"", "\"string\"", "\"binary\"", "\"numeric\"", "\"decimal\"", "\"boolean\"", "\"tinyint\"", "\"integer\"", "\"varchar\"", "\"smallint\"", "\"varbinary\"", "\"timestamp\"", "\"java_object\"", "\"longvarchar\"", "\"longvarbinary\"",
			"\"transaction isolation level\"", "\"auto commit\"", "\"read committed\"", "\"read uncommitted\"", "\"repeatable read\"", "\"serializable\"", "\"cascade\"", "\"current_time\"", "\"current_date\"", "\"current_timestamp\"", "\"like\"", "\"regex\"", "\"and\"", "\"or\"", "\"in\"",
			"\"not\"", "<WITH_NC>", "<FOR_FETCH_ONLY>", "<NUMBER_LITERAL>", "<STRING_LITERAL>", "<QUOTED_VARIABLE>", "<IDENTIFIER>", "<VARIABLE>", "<DOT_DELIMINATED_REF>", "<SLASH_DELIMINATED_REF>", "<QUOTED_DELIMINATED_REF>", "<QUOTED_SLASH_DELIMINATED_REF>", "<JAVA_OBJECT_ARRAY_REF>",
			"<CTALIAS>", "<GLOBVARIABLE>", "<QUOTEDGLOBVARIABLE>", "\"?\"", "<LETTER>", "<DIGIT>", "\";\"", "\"WITH HOLD\"", "\"WITH RETURN\"", "\",\"", "\"(\"", "\")\"", };

}

package test;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import com.ss.speedtransfer.util.parser.sql.SQLParser;
import com.ss.speedtransfer.util.parser.sql.SQLParserHelper;
import com.ss.speedtransfer.util.parser.sql.SQLParserTokenManager;
import com.ss.speedtransfer.util.parser.sql.SQLVisitorFlattener;
import com.ss.speedtransfer.util.parser.sql.SimpleCharStream;
import com.ss.speedtransfer.util.parser.sql.StatementTree;


public class SQLParserTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Map<String, String> vmap = new HashMap<String, String>();
		vmap.put("var1", "12345");
		vmap.put("var2", "Value 2");
		

		try {
			//String sql = "SELECT col1 FROM mytable  WHERE col2 >     (SELECT orderno FROM mytable1      WHERE backorder ='Y')";
			String sql = "SELECT col1 FROM mytable  WHERE col2 >     SELECT orderno FROM mytable1      WHERE col = 125 GROUP BY col1 HAVING havcol > '|var2|'";
			//String sql = "SELECT col1 FROM mytable  WHERE GROUP = BY and backorder = var2 and berry = 'var1'";
			
//			SQLParserHelper parser = new SQLParserHelper(sql);
//			parser.parse();
//			parser.getFilterExpression();
//			parser.getHavingExpression();
			
			ByteArrayInputStream in = new ByteArrayInputStream(sql.getBytes());
			SimpleCharStream stream = new SimpleCharStream(in, 1, 1);
			SQLParserTokenManager lexer = new SQLParserTokenManager(stream);
			SQLParser parser = new SQLParser(lexer);
			StatementTree tree = parser.Statement();
			SQLReplacementVariableTranslationVisitor visitor = new SQLReplacementVariableTranslationVisitor(vmap);
			tree.acceptVisitor(visitor,null);
			System.out.println(visitor.getSQL());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

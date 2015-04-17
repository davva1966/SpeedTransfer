package test;

import java.io.ByteArrayInputStream;

import com.ss.speedtransfer.util.parser.sql.SQLParser;
import com.ss.speedtransfer.util.parser.sql.SQLParserTokenManager;
import com.ss.speedtransfer.util.parser.sql.SQLVisitorFormatFlattener;
import com.ss.speedtransfer.util.parser.sql.SimpleCharStream;
import com.ss.speedtransfer.util.parser.sql.StatementTree;


public class SQLParserTest1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String sql;
		
		sql = "drop table curt.temptable2;";
		parseAndPrint(sql);

//		sql = "Select (col / col2) as rate, CHSPNO , CHSUNO, NANAME, CHDDAT as Dispatch, CHADAT, CHAMOU, amt as LC, CHTGWG, CHTGVL	 from SROLCH join sronam on chsuno = nanum left outer join (select ccspno, sum(CCAMOU) as amt	 from srolcc group by ccspno) as tmp on ccspno = chspno	 where chshps = 60	 and chddat between |FDAT| and |TDAT| order by chspno ";
//		parseAndPrint(sql);
//		sql = "Select ctveno as Supplier, naname, grpcur as Curr, grprdc, grcqty, grunit, gramtr as PO_FX_AMT, gramsy as PO_AUD_Amt, grorno as order, grline as line, grspno as shipment, chsdii, GPEDSC as EXRDIFF, ctdoty as DT, ctidno as Inv_no, ctdodt as INV_DATE, cttcoa as INV_AMT_FX, ctscoa as INV_AMT_AUD, CTEXRT as inv_rate from SROGRT join Z3OUDED on grprdc = UDZ3PRCU join srogpx on gpgrky = grgrky join srolta on gprefx = ctrefx join sronam	 on ctveno = nanum left outer join srolch on chspno = grspno where ctdodt >= |FDAT| and ctdodt <= |TDAT| and UDZ3UDRT = 'AUDIT'	 and UDZ3AL01 = 'Y' order by grprdc, ctdodt";
//		parseAndPrint(sql);
//		sql = "select dcz3shpa, dcz3pocd, dcz3ddat, dcz3depo, dcz3parz, count(dcz3pocd), sum(dcz3weig) from z3omdc where DCZ3SHPA = 'SINGLEBOX' and DCZ3DDAT between 20100701 and 20110630 group by dcz3shpa, dcz3pocd, dcz3ddat, dcz3depo, dcz3parz	order by  dcz3shpa, dcz3pocd, dcz3ddat, dcz3depo, dcz3parz";
//		parseAndPrint(sql);
	}

	public static void parseAndPrint(String sql) {

		try {
			ByteArrayInputStream in = new ByteArrayInputStream(sql.getBytes());
			SimpleCharStream stream = new SimpleCharStream(in, 1, 1);
			SQLParserTokenManager lexer = new SQLParserTokenManager(stream);
			SQLParser parser = new SQLParser(lexer);
			StatementTree tree = parser.Statement();
			SQLVisitorFormatFlattener visitor = new SQLVisitorFormatFlattener();
			tree.acceptVisitor(visitor, null);

			System.out.println(visitor.getSQL());
			System.out.println();
			System.out.println();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

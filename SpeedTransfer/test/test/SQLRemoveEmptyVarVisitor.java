package test;

import java.util.List;
import java.util.Stack;

import com.ss.speedtransfer.util.parser.sql.Expression;
import com.ss.speedtransfer.util.parser.sql.FromClause;
import com.ss.speedtransfer.util.parser.sql.FunctionDef;
import com.ss.speedtransfer.util.parser.sql.Operator;
import com.ss.speedtransfer.util.parser.sql.ParameterSubstitution;
import com.ss.speedtransfer.util.parser.sql.SQLVisitor;
import com.ss.speedtransfer.util.parser.sql.SearchExpression;
import com.ss.speedtransfer.util.parser.sql.StatementTree;
import com.ss.speedtransfer.util.parser.sql.TObject;
import com.ss.speedtransfer.util.parser.sql.TType;
import com.ss.speedtransfer.util.parser.sql.TableSelectExpression;
import com.ss.speedtransfer.util.parser.sql.Variable;


public class SQLRemoveEmptyVarVisitor implements SQLVisitor {
	
	Expression newExpression = new Expression();

	@Override
	public boolean visit(StatementTree stmt, Object context) {
		TableSelectExpression exp = stmt.getTableSelectExpression();
		if (exp != null)
			visit(exp, context);
		return true;
	}

	@Override
	public void visitEnd(StatementTree stmt, Object context) {
	}

	@Override
	public boolean visit(TableSelectExpression stmt, Object context) {
		SearchExpression whereExp = (SearchExpression) stmt.getWhere_clause();
		if (whereExp != null && whereExp.getFromExpression() != null)
			whereExp.acceptVisitor(this, context);

		return true;
	}

	@Override
	public void visitEnd(TableSelectExpression stmt, Object context) {

	}

	@Override
	public boolean visit(FromClause stmt, Object context) {
		return true;
	}

	@Override
	public void visitEnd(FromClause stmt, Object context) {
	}

	@Override
	public boolean visit(SearchExpression stmt, Object context) {
		Expression exp = stmt.getFromExpression();
		if (exp != null) 
			visit(exp, new Stack());
		return true;
	}

	@Override
	public void visitEnd(SearchExpression stmt, Object context) {

	}
	
	public boolean visit(Expression stmt, Stack context) {
		
		int size = stmt.size();
		if (size <= 1) {
			Object o = stmt.elementAt(0);
			visitExpressionObject(o, context);
			return true;
		}

		if (size <= 3) {

			if (size <= 2) {
				Object o = stmt.elementAt(0);
				if (o instanceof TableSelectExpression) {
					o = stmt.elementAt(1);
					if (o instanceof Operator) {
						visitExpressionObject(o, context);
						o = stmt.elementAt(0);
						visit((TableSelectExpression) o, context);
					} else {
						throw new RuntimeException("Failed to parse SQL: " + stmt.toString());
					}
				} else {
					visitExpressionObject(o, context);
					o = stmt.elementAt(1);
					visitExpressionObject(o, context);
				}
				return true;

			} else {

				Object o = stmt.elementAt(0);
				Object o2 = stmt.elementAt(1);
				Object o3 = stmt.elementAt(2);

				if (o3 instanceof Operator) {
					visitExpressionObject(o, context);
					visitExpressionObject(o3, context);
					if (o2 instanceof TableSelectExpression) {
						visit((TableSelectExpression) o2, context);
					} else {
						// Here
						visitExpressionObject(o2, context);
					}
					return true;
				}
			}

		}

		Object oper = stmt.last();

		Expression[] exps = stmt.split();
		Expression exp1 = exps[0];
		Expression exp2 = exps[1];

		visit(exp1, context);
		visitExpressionObject(oper, context);
		visit(exp2, context);


		return true;
	}
	
	private void visitExpressionObject(Object stmt, Object context) {
		System.out.println(stmt);
//		if (stmt instanceof Operator) {
//			visit((Operator) stmt, context);
//		} else if (stmt instanceof Variable) {
//			visit((Variable) stmt, context);
//		} else if (stmt instanceof TType) {
//			visit((TType) stmt, context);
//		} else if (stmt instanceof TObject) {
//			visit((TObject) stmt, context);
//		} else if (stmt instanceof FunctionDef) {
//			visit((FunctionDef) stmt, context);
//		} else if (stmt instanceof TableSelectExpression) {
//			visit((TableSelectExpression) stmt, context);
//		}
	}

}

// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>Expression</code> class
 */
public final class Expression {

	/**
	 * The list of elements followed by operators in our expression. The expression elements may be of any type represented by the database (see 'addElement' method for the accepted objects). The
	 * expression operators may be '+', '-', '*', '*', '/', '=', '>=', ' <>', etc (as an Operator object (see the Operator class for details)).
	 * <p>
	 * This list is stored in postfix order.
	 */
	private ArrayList elements = new ArrayList();

	/**
	 * The evaluation stack for when the expression is evaluated.
	 */
	private transient ArrayList eval_stack;

	/**
	 * The expression as a plain human readable string. This is in a form that can be readily parsed to an Expression object.
	 */
	private StringBuffer text;

	/**
	 * The expression as a plain human readable string. This is in a form that can be readily parsed to an Expression object.
	 */
	private StringBuffer xttext;

	/**
	 * Constructs a new Expression.
	 */
	public Expression() {
		text = new StringBuffer();
		xttext = new StringBuffer();
	}

	/**
	 * Constructs a new Expression with a single object element.
	 */
	public Expression(Object ob) {
		this();
		addElement(ob);
	}

	/**
	 * Constructs a copy of the given Expression.
	 */
	public Expression(Expression exp) {
		concat(exp);
		text = new StringBuffer(new String(exp.text));
	}

	/**
	 * Constructs a new Expression from the concatination of expression1 and expression2 and the operator for them.
	 */
	public Expression(Expression exp1, Operator op, Expression exp2) {
		// Remember, this is in postfix notation.
		elements.addAll(exp1.elements);
		elements.addAll(exp2.elements);
		elements.add(op);
	}

	/**
	 * Returns the StringBuffer that we can use to append plain text representation as we are parsing the expression.
	 */
	public StringBuffer text() {
		return text;
	}

	/**
	 * Returns the StringBuffer that we can use to append plain text representation as we are parsing the expression but embedded variables has syntax |A| instead of :A.
	 */
	public StringBuffer xttext() {
		return xttext;
	}

	/**
	 * Copies the text from the given expression.
	 */
	public void copyTextFrom(Expression e) {
		this.text = new StringBuffer(new String(e.text()));
	}

	/**
	 * Generates a simple expression from two objects and an operator.
	 */
	public static Expression simple(Object ob1, Operator op, Object ob2) {
		Expression exp = new Expression(ob1);
		exp.addElement(ob2);
		exp.addElement(op);
		return exp;
	}

	/**
	 * Adds a new element into the expression. String, BigNumber, Boolean and Variable are the types of elements allowed.
	 * <p>
	 * Must be added in postfix order.
	 */
	public void addElement(Object ob) {
		if (ob == null) {
			elements.add(TObject.nullVal());
		} else if (ob instanceof TObject || ob instanceof ParameterSubstitution || ob instanceof Variable || ob instanceof FunctionDef || ob instanceof Operator || ob instanceof StatementTree || ob instanceof TableSelectExpression) {
			elements.add(ob);
		} else {
			throw new Error("Unknown element type added to expression: " + ob.getClass());
		}
	}

	/**
	 * Merges an expression with this expression. For example, given the expression 'ab', if the expression 'abc+-' was added the expression would become 'ababc+-'.
	 * <p>
	 * This method is useful when copying parts of other expressions when forming an expression.
	 * <p>
	 * This always returns this expression. This does not change 'text()'.
	 */
	public Expression concat(Expression expr) {
		elements.addAll(expr.elements);
		return this;
	}

	/**
	 * Adds a new operator into the expression. Operators are represented as an Operator (eg. ">", "+", " < <", "!=" )
	 * <p>
	 * Must be added in postfix order.
	 */
	public void addOperator(Operator op) {
		elements.add(op);
	}

	/**
	 * Returns the number of elements and operators that are in this postfix list.
	 */
	public int size() {
		return elements.size();
	}

	/**
	 * Returns the element at the given position in the postfix list. Note, this can return Operator's.
	 */
	public Object elementAt(int n) {
		return elements.get(n);
	}

	/**
	 * Returns the element at the end of the postfix list (the last element).
	 */
	public Object last() {
		return elements.get(size() - 1);
	}

	/**
	 * Sets the element at the given position in the postfix list. This should be called after the expression has been setup to alter variable alias names, etc.
	 */
	public void setElementAt(int n, Object ob) {
		elements.set(n, ob);
	}

	/**
	 * Pushes an element onto the evaluation stack.
	 */
	private final void push(Object ob) {
		eval_stack.add(ob);
	}

	/**
	 * Pops an element from the evaluation stack.
	 */
	private final Object pop() {
		return eval_stack.remove(eval_stack.size() - 1);
	}

	/**
	 * Returns a complete List of Variable objects in this expression not including correlated variables.
	 */
	public List allVariables() {
		ArrayList vars = new ArrayList();
		for (int i = 0; i < elements.size(); ++i) {
			Object ob = elements.get(i);
			if (ob instanceof Variable) {
				vars.add(ob);
			} else if (ob instanceof FunctionDef) {
				Expression[] params = ((FunctionDef) ob).getParameters();
				for (int n = 0; n < params.length; ++n) {
					vars.addAll(params[n].allVariables());
				}
			} else if (ob instanceof TObject) {
				TObject tob = (TObject) ob;
				if (tob.getTType() instanceof TArrayType) {
					Expression[] exp_list = (Expression[]) tob.getObject();
					for (int n = 0; n < exp_list.length; ++n) {
						vars.addAll(exp_list[n].allVariables());
					}
				}
			}
		}
		return vars;
	}

	/**
	 * Returns a complete list of all element objects that are in this expression and in the parameters of the functions of this expression.
	 */
	public List allElements() {
		ArrayList elems = new ArrayList();
		for (int i = 0; i < elements.size(); ++i) {
			Object ob = elements.get(i);
			if (ob instanceof Operator) {
			} else if (ob instanceof FunctionDef) {
				Expression[] params = ((FunctionDef) ob).getParameters();
				for (int n = 0; n < params.length; ++n) {
					elems.addAll(params[n].allElements());
				}
			} else if (ob instanceof TObject) {
				TObject tob = (TObject) ob;
				if (tob.getTType() instanceof TArrayType) {
					Expression[] exp_list = (Expression[]) tob.getObject();
					for (int n = 0; n < exp_list.length; ++n) {
						elems.addAll(exp_list[n].allElements());
					}
				} else {
					elems.add(ob);
				}
			} else {
				elems.add(ob);
			}
		}
		return elems;
	}

	/**
	 * Returns true if the expression doesn't include any variables or non constant functions (is constant). Note that a correlated variable is considered a constant.
	 */
	public boolean isConstant() {
		for (int n = 0; n < elements.size(); ++n) {
			Object ob = elements.get(n);
			if (ob instanceof TObject) {
				TObject tob = (TObject) ob;
				TType ttype = tob.getTType();
				// If this is a query plan, return false
				if (ttype instanceof TQueryPlanType) {
					return false;
				}
				// If this is an array, check the array for constants
				else if (ttype instanceof TArrayType) {
					Expression[] exp_list = (Expression[]) tob.getObject();
					for (int p = 0; p < exp_list.length; ++p) {
						if (!exp_list[p].isConstant()) {
							return false;
						}
					}
				}
			} else if (ob instanceof Variable) {
				return false;
			} else if (ob instanceof FunctionDef) {
				Expression[] params = ((FunctionDef) ob).getParameters();
				for (int p = 0; p < params.length; ++p) {
					if (!params[p].isConstant()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Returns true if the expression has a subquery (eg 'in ( select ... )') somewhere in it (this cascades through function parameters also).
	 */
	public boolean hasSubQuery() {
		List list = allElements();
		int len = list.size();
		for (int n = 0; n < len; ++n) {
			Object ob = list.get(n);
			if (ob instanceof TObject) {
				TObject tob = (TObject) ob;
				if (tob.getTType() instanceof TQueryPlanType) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if the expression contains a NOT operator somewhere in it.
	 */
	public boolean containsNotOperator() {
		for (int n = 0; n < elements.size(); ++n) {
			Object ob = elements.get(n);
			if (ob instanceof Operator) {
				if (((Operator) ob).isNot()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if the expression contains a OR operator somewhere in it.
	 */
	public boolean containsOrOperator() {
		for (int n = 0; n < elements.size(); ++n) {
			Object ob = elements.get(n);
			if (ob instanceof Operator.OrOperator) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the Variable if this expression evaluates to a single variable, otherwise returns null. A correlated variable will not be returned.
	 */
	public Variable getVariable() {
		Object ob = elementAt(0);
		if (size() == 1 && ob instanceof Variable) {
			return (Variable) ob;
		}
		return null;
	}

	/**
	 * Returns an array of two Expression objects that represent the left hand and right and side of the last operator in the post fix notation. For example, (a + b) - (c + d) will return { (a + b),
	 * (c + d) }. Or more a more useful example is;
	 * <p>
	 * 
	 * <pre>
	 * 
	 *    id + 3 &gt; part_id - 2 will return ( id + 3, part_id - 2 }
	 * 
	 * </pre>
	 */
	public Expression[] split() {
		if (size() <= 1) {
			throw new Error("Can only split expressions with more than 1 element.");
		}

		int midpoint = -1;
		int stack_size = 0;
		for (int n = 0; n < size() - 1; ++n) {
			Object ob = elementAt(n);
			if (ob instanceof Operator) {
				--stack_size;
			} else {
				++stack_size;
			}

			if (stack_size == 1) {
				midpoint = n;
			}
		}

		if (midpoint == -1) {
			throw new Error("postfix format error: Midpoint not found.");
		}

		Expression lhs = new Expression();
		for (int n = 0; n <= midpoint; ++n) {
			lhs.addElement(elementAt(n));
		}

		Expression rhs = new Expression();
		for (int n = midpoint + 1; n < size() - 1; ++n) {
			rhs.addElement(elementAt(n));
		}

		return new Expression[] { lhs, rhs };
	}

	/**
	 * Returns the end Expression of this expression. For example, an expression of 'ab' has an end expression of 'b'. The expression 'abc+=' has an end expression of 'abc+='.
	 * <p>
	 * This is a useful method to call in the middle of an Expression object being formed. It allows for the last complete expression to be returned.
	 * <p>
	 * If this is called when an expression is completely formed it will always return the complete expression.
	 */
	public Expression getEndExpression() {

		int stack_size = 1;
		int end = size() - 1;
		for (int n = end; n > 0; --n) {
			Object ob = elementAt(n);
			if (ob instanceof Operator) {
				++stack_size;
			} else {
				--stack_size;
			}

			if (stack_size == 0) {
				// Now, n .. end represents the new expression.
				Expression new_exp = new Expression();
				for (int i = n; i <= end; ++i) {
					new_exp.addElement(elementAt(i));
				}
				return new_exp;
			}
		}

		return new Expression(this);
	}

	/**
	 * Breaks this expression into a list of sub-expressions that are split by the given operator. For example, given the expression;
	 * <p>
	 * 
	 * <pre>
	 * 
	 *    (a = b AND b = c AND (a = 2 OR c = 1))
	 * 
	 * </pre>
	 * 
	 * <p>
	 * Calling this method with logical_op = "and" will return a list of the three expressions.
	 * <p>
	 * This is a common function used to split up an expressions into logical components for processing.
	 */
	public ArrayList breakByOperator(ArrayList list, final String logical_op) {
		// The last operator must be 'and'
		Object ob = last();
		if (ob instanceof Operator) {
			Operator op = (Operator) ob;
			if (op.is(logical_op)) {
				// Last operator is 'and' so split and recurse.
				Expression[] exps = split();
				list = exps[0].breakByOperator(list, logical_op);
				list = exps[1].breakByOperator(list, logical_op);
				return list;
			}
		}
		// If no last expression that matches then add this expression to the
		// list.
		list.add(this);
		return list;
	}

	/**
	 * Performs a deep clone of this object, calling 'clone' on any elements that are mutable or shallow copying immutable members.
	 */
	public Object clone() throws CloneNotSupportedException {
		// Shallow clone
		Expression v = (Expression) super.clone();
		v.eval_stack = null;
		// v.text = new StringBuffer(new String(text));
		int size = elements.size();
		ArrayList cloned_elements = new ArrayList(size);
		v.elements = cloned_elements;

		return v;
	}

	/**
	 * Returns a string representation of this object for diagnostic purposes.
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("[ Expression ");
		if (text() != null) {
			buf.append("[");
			buf.append(text().toString());
			buf.append("]");
		}
		buf.append(": ");
		for (int n = 0; n < elements.size(); ++n) {
			buf.append(elements.get(n));
			if (n < elements.size() - 1) {
				buf.append(",");
			}
		}
		buf.append(" ]");
		return new String(buf);
	}

}
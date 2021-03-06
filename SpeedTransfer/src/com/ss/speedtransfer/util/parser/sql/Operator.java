// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

import java.util.HashMap;

/**
 * The <code>Operator</code> class
 */
public abstract class Operator implements java.io.Serializable {

	static final long serialVersionUID = 516615288995154064L;

	// ---------- Statics ----------

	/**
	 * The ANY and ALL enumerator.
	 */
	public static final int NONE = 0, ANY = 1, ALL = 2;

	// ---------- Member ----------

	/**
	 * A string that represents this operator.
	 */
	private String op;

	/**
	 * If this is a set operator such as ANY or ALL then this is set with the flag type.
	 */
	private int set_type;

	/**
	 * The precedence of this operator.
	 */
	private int precedence;

	/**
	 * Constructs the Operator.
	 */
	protected Operator(String op) {
		this(op, 0, NONE);
	}

	protected Operator(String op, int precedence) {
		this(op, precedence, NONE);
	}

	protected Operator(String op, int precedence, int set_type) {
		if (set_type != NONE && set_type != ANY && set_type != ALL) {
			throw new Error("Invalid set_type.");
		}
		this.op = op;
		this.precedence = precedence;
		this.set_type = set_type;
	}

	/**
	 * Returns true if this operator is equal to the operator string.
	 */
	public boolean is(String given_op) {
		return given_op.equals(op);
	}

	public int precedence() {
		return precedence;
	}

	public boolean isCondition() {
		return (equals(eq_op) || equals(neq_op) || equals(g_op) || equals(l_op) || equals(geq_op) || equals(leq_op) || equals(is_op) || equals(isn_op));
	}

	public boolean isMathematical() {
		return (equals(add_op) || equals(sub_op) || equals(mul_op) || equals(div_op) || equals(concat_op));
	}

	public boolean isPattern() {
		return (equals(like_op) || equals(nlike_op) || equals(regex_op));
	}

	public boolean isLogical() {
		return (equals(and_op) || equals(or_op));
	}

	public boolean isNot() {
		return equals(not_op);
	}

	public boolean isSubQuery() {
		return (set_type != NONE || equals(in_op) || equals(nin_op) || equals(exists_op));
	}

	/**
	 * Returns an Operator that is the reverse of this Operator. This is used for reversing a conditional expression. eg. 9 > id becomes id < 9.
	 */
	public Operator reverse() {
		if (equals(eq_op) || equals(neq_op) || equals(is_op) || equals(isn_op)) {
			return this;
		} else if (equals(g_op)) {
			return l_op;
		} else if (equals(l_op)) {
			return g_op;
		} else if (equals(geq_op)) {
			return leq_op;
		} else if (equals(leq_op)) {
			return geq_op;
		}
		throw new Error("Can't reverse a non conditional operator.");
	}

	/**
	 * Returns true if this operator is not inversible.
	 */
	public boolean isNotInversible() {
		// The REGEX op, and mathematical operators are not inversible.
		return equals(regex_op) || isMathematical();
	}

	/**
	 * Returns the inverse operator of this operator. For example, = becomes <>, > becomes <=, AND becomes OR.
	 */
	public Operator inverse() {
		if (isSubQuery()) {
			int inv_type;
			if (isSubQueryForm(ANY)) {
				inv_type = ALL;
			} else if (isSubQueryForm(ALL)) {
				inv_type = ANY;
			} else {
				throw new RuntimeException("Can not handle sub-query form.");
			}

			Operator inv_op = Operator.get(op).inverse();

			return inv_op.getSubQueryForm(inv_type);
		} else if (equals(eq_op)) {
			return neq_op;
		} else if (equals(neq_op)) {
			return eq_op;
		} else if (equals(g_op)) {
			return leq_op;
		} else if (equals(l_op)) {
			return geq_op;
		} else if (equals(geq_op)) {
			return l_op;
		} else if (equals(leq_op)) {
			return g_op;
		} else if (equals(and_op)) {
			return or_op;
		} else if (equals(or_op)) {
			return and_op;
		} else if (equals(like_op)) {
			return nlike_op;
		} else if (equals(nlike_op)) {
			return like_op;
		} else if (equals(is_op)) {
			return isn_op;
		} else if (equals(isn_op)) {
			return is_op;
		} else {
			throw new Error("Can't inverse operator '" + op + "'");
		}

	}

	/**
	 * Given a parameter of either NONE, ANY, ALL or SINGLE, this returns true if this operator is of the given type.
	 */
	public boolean isSubQueryForm(int type) {
		return type == set_type;
	}

	/**
	 * Returns the sub query representation of this operator.
	 */
	int getSubQueryFormRepresentation() {
		return set_type;
	}

	/**
	 * Returns the ANY or ALL form of this operator.
	 */
	public Operator getSubQueryForm(int type) {
		Operator result_op = null;
		if (type == ANY) {
			result_op = (Operator) any_map.get(op);
		} else if (type == ALL) {
			result_op = (Operator) all_map.get(op);
		} else if (type == NONE) {
			result_op = get(op);
		}

		if (result_op == null) {
			throw new Error("Couldn't change the form of operator '" + op + "'.");
		}
		return result_op;
	}

	/**
	 * Same as above only it handles the type as a string.
	 */
	public Operator getSubQueryForm(String type_str) {
		String s = type_str.toUpperCase();
		if (s.equals("SINGLE") || s.equals("ANY") || s.equals("SOME")) {
			return getSubQueryForm(ANY);
		} else if (s.equals("ALL")) {
			return getSubQueryForm(ALL);
		}
		throw new Error("Do not understand subquery type '" + type_str + "'");
	}

	/**
	 * The type of object this Operator evaluates to.
	 */
	public TType returnTType() {
		if (equals(concat_op)) {
			return TType.STRING_TYPE;
		} else if (isMathematical()) {
			return TType.NUMERIC_TYPE;
		} else {
			return TType.BOOLEAN_TYPE;
		}
	}

	/**
	 * Returns the string value of this operator.
	 */
	String stringRepresentation() {
		return op;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(op);
		if (set_type == ANY) {
			buf.append(" ANY");
		} else if (set_type == ALL) {
			buf.append(" ALL");
		}
		return new String(buf);
	}

	public boolean equals(Object ob) {
		if (this == ob)
			return true;
		Operator oob = (Operator) ob;
		return op.equals(oob.op) && set_type == oob.set_type;
	}

	/**
	 * Returns an Operator with the given string.
	 */
	public static Operator get(String op) {
		if (op.equals("+")) {
			return add_op;
		} else if (op.equals("-")) {
			return sub_op;
		} else if (op.equals("*")) {
			return mul_op;
		} else if (op.equals("/")) {
			return div_op;
		} else if (op.equals("||")) {
			return concat_op;
		}

		else if (op.equals("=") | op.equals("==")) {
			return eq_op;
		} else if (op.equals("<>") | op.equals("!=")) {
			return neq_op;
		} else if (op.equals(">")) {
			return g_op;
		} else if (op.equals("<")) {
			return l_op;
		} else if (op.equals(">=")) {
			return geq_op;
		} else if (op.equals("<=")) {
			return leq_op;
		}

		else if (op.equals("(")) {
			return par1_op;
		} else if (op.equals(")")) {
			return par2_op;
		}

		// Operators that are words, convert to lower case...
		op = op.toLowerCase();
		if (op.equals("is")) {
			return is_op;
		} else if (op.equals("is not")) {
			return isn_op;
		} else if (op.equals("like")) {
			return like_op;
		} else if (op.equals("not like")) {
			return nlike_op;
		} else if (op.equals("regex")) {
			return regex_op;
		}

		else if (op.equals("exists")) {
			return exists_op;
		} else if (op.equals("in")) {
			return in_op;
		} else if (op.equals("not in")) {
			return nin_op;
		}

		else if (op.equals("not")) {
			return not_op;
		} else if (op.equals("and")) {
			return and_op;
		} else if (op.equals("or")) {
			return or_op;
		}

		throw new Error("Unrecognised operator type: " + op);
	}

	// ---------- Convenience methods ----------

	/**
	 * Returns true if the given TObject is a boolean and is true. If the TObject is not a boolean value or is null or is false, then it returns false.
	 */
	private static boolean isTrue(TObject bool) {
		return (!bool.isNull() && bool.getTType() instanceof TBooleanType && bool.getObject().equals(Boolean.TRUE));
	}

	// ---------- The different types of operator's we can have ----------

	private final static AddOperator add_op = new AddOperator();

	private final static SubtractOperator sub_op = new SubtractOperator();

	private final static MultiplyOperator mul_op = new MultiplyOperator();

	private final static DivideOperator div_op = new DivideOperator();

	private final static ConcatOperator concat_op = new ConcatOperator();

	private final static EqualOperator eq_op = new EqualOperator();

	private final static NotEqualOperator neq_op = new NotEqualOperator();

	private final static GreaterOperator g_op = new GreaterOperator();

	private final static LesserOperator l_op = new LesserOperator();

	private final static GreaterEqualOperator geq_op = new GreaterEqualOperator();

	private final static LesserEqualOperator leq_op = new LesserEqualOperator();

	private final static IsOperator is_op = new IsOperator();

	private final static IsNotOperator isn_op = new IsNotOperator();

	private final static PatternMatchTrueOperator like_op = new PatternMatchTrueOperator();

	private final static PatternMatchFalseOperator nlike_op = new PatternMatchFalseOperator();

	private final static RegexOperator regex_op = new RegexOperator();

	private final static Operator exists_op = new ExistsOperator();

	private final static Operator in_op;

	private final static Operator nin_op;

	private final static Operator not_op = new SimpleOperator("not", 3);

	private final static AndOperator and_op = new AndOperator();

	private final static OrOperator or_op = new OrOperator();

	private final static ParenOperator par1_op = new ParenOperator("(");

	private final static ParenOperator par2_op = new ParenOperator(")");

	// Maps from operator to 'any' operator
	private final static HashMap any_map = new HashMap();

	// Maps from operator to 'all' operator.
	private final static HashMap all_map = new HashMap();

	static {
		// Populate the static ANY and ALL mapping
		any_map.put("=", new AnyOperator("="));
		any_map.put("<>", new AnyOperator("<>"));
		any_map.put(">", new AnyOperator(">"));
		any_map.put(">=", new AnyOperator(">="));
		any_map.put("<", new AnyOperator("<"));
		any_map.put("<=", new AnyOperator("<="));

		all_map.put("=", new AllOperator("="));
		all_map.put("<>", new AllOperator("<>"));
		all_map.put(">", new AllOperator(">"));
		all_map.put(">=", new AllOperator(">="));
		all_map.put("<", new AllOperator("<"));
		all_map.put("<=", new AllOperator("<="));

		// The IN and NOT IN operator are '= ANY' and '<> ALL' respectively.
		in_op = (Operator) any_map.get("=");
		nin_op = (Operator) all_map.get("<>");
	}

	static class ExistsOperator extends Operator {

		static final long serialVersionUID = -4605268759294925687L;

		public ExistsOperator() {
			super("exists", 8, ANY);
		}
	}

	static class AddOperator extends Operator {

		static final long serialVersionUID = 6995379384325694391L;

		public AddOperator() {
			super("+", 10);
		}
	};

	static class SubtractOperator extends Operator {

		static final long serialVersionUID = 3035882496296296786L;

		public SubtractOperator() {
			super("-", 15);
		}
	};

	static class MultiplyOperator extends Operator {

		static final long serialVersionUID = 8191233936463163847L;

		public MultiplyOperator() {
			super("*", 20);
		}
	};

	static class DivideOperator extends Operator {

		static final long serialVersionUID = -2695205152105036247L;

		public DivideOperator() {
			super("/", 20);
		}
	};

	static class ConcatOperator extends Operator {

		public ConcatOperator() {
			super("||", 10);
		}
	};

	public static class EqualOperator extends Operator {

		static final long serialVersionUID = -5022271093834866261L;

		public EqualOperator() {
			super("=", 4);
		}
	}

	public static class NotEqualOperator extends Operator {

		static final long serialVersionUID = 5868174826733282297L;

		public NotEqualOperator() {
			super("<>", 4);
		}
	}

	public static class GreaterOperator extends Operator {

		static final long serialVersionUID = -6870425685250387549L;

		public GreaterOperator() {
			super(">", 4);
		}
	}

	public static class LesserOperator extends Operator {

		static final long serialVersionUID = 2962736161551360032L;

		public LesserOperator() {
			super("<", 4);
		}
	}

	public static class GreaterEqualOperator extends Operator {

		static final long serialVersionUID = 6040843932499067476L;

		public GreaterEqualOperator() {
			super(">=", 4);
		}
	}

	public static class LesserEqualOperator extends Operator {

		static final long serialVersionUID = 4298966494510169621L;

		public LesserEqualOperator() {
			super("<=", 4);
		}
	}

	static class IsOperator extends Operator {

		static final long serialVersionUID = -5537856102106541908L;

		public IsOperator() {
			super("is", 4);
		}
	}

	static class IsNotOperator extends Operator {

		static final long serialVersionUID = 1224184162192790982L;

		public IsNotOperator() {
			super("is not", 4);
		}
	}

	static class AnyOperator extends Operator {

		static final long serialVersionUID = 6421321961221271735L;

		public AnyOperator(String op) {
			super(op, 8, ANY);
		}
	}

	static class AllOperator extends Operator {

		static final long serialVersionUID = -4605268759294925687L;

		public AllOperator(String op) {
			super(op, 8, ALL);
		}
	}

	static class RegexOperator extends Operator {

		static final long serialVersionUID = 8062751421429261272L;

		public RegexOperator() {
			super("regex", 8);
		}
	}

	static class PatternMatchTrueOperator extends Operator {

		static final long serialVersionUID = 3038856811053114238L;

		public PatternMatchTrueOperator() {
			super("like", 8);
		}
	}

	static class PatternMatchFalseOperator extends Operator {

		static final long serialVersionUID = 7271394661743778291L;

		public PatternMatchFalseOperator() {
			super("not like", 8);
		}
	}

	// and/or have lowest precedence
	public static class AndOperator extends Operator {

		static final long serialVersionUID = -6044610739300316190L;

		public AndOperator() {
			super("and", 2);
		}
	}

	public static class OrOperator extends Operator {

		static final long serialVersionUID = 6505549460035023998L;

		public OrOperator() {
			super("or", 1);
		}
	}

	static class ParenOperator extends Operator {

		static final long serialVersionUID = -5720902399037456435L;

		public ParenOperator(String paren) {
			super(paren);
		}
	}

	static class SimpleOperator extends Operator {

		static final long serialVersionUID = 1136249637094226133L;

		public SimpleOperator(String str) {
			super(str);
		}

		public SimpleOperator(String str, int prec) {
			super(str, prec);
		}
	}

}